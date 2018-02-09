/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
import java.util.Date

@WorkflowBuilder
class PresentationParametersBuilder(
    private val steps: MutableList<PresentationStep>,
    parameters: MutableList<ParameterInfo>,
    allParameters: MutableList<ParameterInfo>,
    private val outputParameters: MutableList<ParameterInfo>,
    private val attributes: MutableList<Attribute>
) : ParameterAggregator(parameters, allParameters) {

    fun step(title: String, setup: ParameterAggregator.() -> Unit) {
        val stepParameters = mutableListOf<ParameterInfo>()
        val aggregator = ParameterAggregator(stepParameters, allParameters)
        aggregator.setup()
        steps += PresentationStep.fromParameters(title, stepParameters.asPresentationParameters, aggregator.description, aggregator.qualifiers)
    }

    fun groups(title: String, setup: PresentationGroupBuilder.() -> Unit) {
        val stepGroups = mutableListOf<PresentationGroup>()
        val groupBuilder = PresentationGroupBuilder(stepGroups, allParameters)
        groupBuilder.setup()
        steps += PresentationStep.fromGroups(title, stepGroups, groupBuilder.description, groupBuilder.qualifiers)
    }

    fun output(name: String, type: Reference, setup: OutputParameterBuilder.() -> Unit) {
        outputParameters += OutputParameterBuilder(name, type).apply(setup).buildOutputParameter()
    }

    fun attribute(name: String, type: Reference, setup: AttributeBuilder.() -> Unit) {
        attributes += AttributeBuilder(name, type).apply(setup).buildAttribute()
    }
}

@WorkflowBuilder
class PresentationGroupBuilder(
    private val groups: MutableList<PresentationGroup>,
    private val allParameters: MutableList<ParameterInfo>
) : BasicBuilder() {

    fun group(title: String, setup: ParameterAggregator.() -> Unit) {
        val groupParameters = mutableListOf<ParameterInfo>()
        val aggregator = ParameterAggregator(groupParameters, allParameters)
        aggregator.setup()
        groups += PresentationGroup(title, groupParameters.asPresentationParameters, aggregator.description, aggregator.qualifiers)
    }

    val qualifiers get() = basicQualifiers
}

@WorkflowBuilder
@Suppress("UNUSED_PARAMETER")
open class ParameterAggregator(
    private val parameters: MutableList<ParameterInfo>,
    protected val allParameters: MutableList<ParameterInfo>
) : BasicBuilder() {

    fun parameter(name: String, type: boolean, setup: BooleanParameterBuilder.() -> Unit) =
        BooleanParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: number, setup: IntParameterBuilder.() -> Unit) =
        IntParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: string, setup: StringParameterBuilder.() -> Unit) =
        StringParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: SecureString, setup: SecureStringParameterBuilder.() -> Unit) =
        SecureStringParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: date, setup: DateParameterBuilder.() -> Unit) =
        DateParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: Reference, setup: ReferenceParameterBuilder.() -> Unit) {
        ReferenceParameterBuilder(name, type).updateWith(setup)
    }

    @JvmName("parameterArrayPair")
    fun parameter(name: String, type: array<Pair<String, String>>, setup: ArrayPairParameterBuilder.() -> Unit) {
        ArrayPairParameterBuilder(name, type).updateWith(setup)
    }

    @JvmName("parameterArrayReference")
    fun parameter(name: String, type: array<Reference>, setup: ReferenceArrayParameterBuilder.() -> Unit = {}) {
        ReferenceArrayParameterBuilder(name, type).updateWith(setup)
    }

    fun parameter(name: String, type: Class<*>, setup: BasicParameterBuilder<*>.() -> Unit) = when (type) {
        java.lang.Boolean::class.java, java.lang.Boolean.TYPE -> {
            BooleanParameterBuilder(name).updateWith(setup)
        }
        String::class.java -> {
            StringParameterBuilder(name).updateWith(setup)
        }
        java.lang.Integer::class.java, java.lang.Integer.TYPE,
        java.lang.Long::class.java, java.lang.Long.TYPE -> {
            IntParameterBuilder(name).updateWith(setup)
        }
        Date::class.java -> {
            DateParameterBuilder(name).updateWith(setup)
        }
        else -> throw UnsupportedOperationException("Unsupported parameter class: ${type.simpleName}")
    }

    val qualifiers get() = basicQualifiers

    private fun <Builder : BasicParameterBuilder<T>, T : Any> Builder.updateWith(setup: Builder.() -> Unit) =
        apply(setup).appendParameterLists()

    private fun BasicParameterBuilder<*>.appendParameterLists() {
        parameterInfo.also {
            parameters.add(it)
            allParameters.add(it)
        }
    }
}

abstract class BasicBuilder {
    var description: String? = null
    var visibility: VisibilityCondition = AlwaysVisible

    protected val basicQualifiers get() = mutableListOf<ParameterQualifier>().apply {
        visibility.let {
            when (it) {
                is AlwaysVisible -> Unit
                else -> add(visibilityConditionQualifier(it))
            }
        }
    }
}

abstract class BasicParameterBuilder<Type: Any>(val parameterName: String, val type: ParameterType<Type>) : BasicBuilder() {
    var mandatory: Boolean = false
    var defaultValue: Type? = null
    var dataBinding: DataBinding<Type> = NoDataBinding
    // TODO: Unify predefinedAnswers and predefinedAnswersAction
    var predefinedAnswers: List<Type>? = null
    var predefinedAnswersAction: ActionCall? = null
    val additionalQualifiers = mutableListOf<ParameterQualifier>()

    val parameterInfo get() = ParameterInfo(
        name = parameterName,
        type = type,
        description = description,
        qualifiers = allQualifiers
    )

    private val allQualifiers get(): List<ParameterQualifier> =
        basicQualifiers + commonQualifiers + customQualifiers + additionalQualifiers

    private val commonQualifiers get() = mutableListOf<ParameterQualifier>().apply {
        if (mandatory) add(mandatoryQualifier)

        defaultValue?.let {
            add(defaultValueQualifier(type, it))
        }
        dataBinding.qualifier?.let {
            add(it)
        }
        predefinedAnswers?.let {
            add(predefinedAnswersQualifier(type, it))
        }
        predefinedAnswersAction?.let {
            add(predefinedAnswersActionQualifier(type, it))
        }
    }

    protected open val customQualifiers: List<ParameterQualifier> get() =
        emptyList()
}

class ArrayPairParameterBuilder(name: String, type: array<Pair<String, String>>) :
        BasicParameterBuilder<List<Pair<String, String>>>(name, type)

class BooleanParameterBuilder(name: String) : BasicParameterBuilder<Boolean>(name, boolean)

class IntParameterBuilder(name: String) : BasicParameterBuilder<Long>(name, number) {
    var min: Long? = null
    var max: Long? = null

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        qualifiers.add(numberFormatQualifier("#0"))
        min?.let {
            qualifiers.add(minNumberValueQualifier(it))
        }
        max?.let {
            qualifiers.add(maxNumberValueQualifier(it))
        }
        return qualifiers
    }
}

class StringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, string) {
    var customValidation: StringValidation? = null
    var multiline: Boolean = false

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        customValidation?.let {
            when (it) {
                is CIDR -> qualifiers.add(cidrValidatorQualifier(parameterName, it.actionName))
                is AllocationPool -> qualifiers.add(allocValidatorQualifier(parameterName, it.cidr, it.actionName))
                is InCIDR -> qualifiers.add(inCidrValidatorQualifier(parameterName, it.cidr, it.actionName))
                is FreeInCIDR -> qualifiers.add(freeInCidrValidatorQualifier(parameterName, it.cidr, it.pools,
                        it.dns, it.actionName))
            }
        }
        if (multiline) {
            qualifiers.add(multilineQualifier())
        }
        return qualifiers
    }
}

class SecureStringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, SecureString)

class DateParameterBuilder(name: String) : BasicParameterBuilder<Date>(name, date)

class ReferenceParameterBuilder(name: String, type: Reference) : BasicParameterBuilder<Reference>(name, type) {

    var showInInventory: Boolean = false
    var listedBy: ActionCall? = null
    var browserRoot: InventoryBrowserRoot = DefaultBrowserRoot

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        if (showInInventory) qualifiers.add(showInInventoryQualifier)
        listedBy?.let {
            qualifiers.add(listFromAction(it, type))
        }
        browserRoot.ognl?.let { qualifiers.add(displayParentFrom(it)) }
        return qualifiers
    }
}

class ReferenceArrayParameterBuilder(name: String, type: array<Reference>) : BasicParameterBuilder<List<Reference>>(name, type)

class OutputParameterBuilder(val name: String, val type: ParameterType<Any>) {
    var description: String? = null

    fun buildOutputParameter() =
        ParameterInfo(name, type, description = description)
}

class AttributeBuilder(val name: String, val type: ParameterType<Any>) {
    var description: String? = null
    var readOnly: Boolean = false

    fun buildAttribute() =
        Attribute(name, type, description, readOnly)
}