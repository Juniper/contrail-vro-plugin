/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.schema.Constraint
import java.util.Date

@WorkflowBuilder
class PresentationParametersBuilder(
    private val steps: MutableList<PresentationStep>,
    parameters: MutableList<ParameterInfo>,
    allParameters: MutableList<ParameterInfo>,
    private val outputParameters: MutableList<ParameterInfo>,
    private val attributes: MutableList<AttributeDefinition>
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

    @JvmName("parameterPairArray")
    fun parameter(name: String, type: array<Pair<String, String>>, setup: ArrayPairParameterBuilder.() -> Unit) {
        ArrayPairParameterBuilder(name, type).updateWith(setup)
    }

    @JvmName("parameterReferenceArray")
    fun parameter(name: String, type: array<Reference>, setup: ReferenceArrayParameterBuilder.() -> Unit = {}) {
        ReferenceArrayParameterBuilder(name, type).updateWith(setup)
    }

    @JvmName("parameterStringArray")
    fun parameter(name: String, type: array<String>, setup: ArrayPrimitiveParameterBuilder<String>.() -> Unit) {
        ArrayPrimitiveParameterBuilder(name, type).updateWith(setup)
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
                AlwaysVisible -> Unit
                else -> add(visibilityConditionQualifier(it))
            }
        }
    }
}

abstract class BasicParameterBuilder<Type: Any>(val parameterName: String, val type: ParameterType<Type>) : BasicBuilder() {
    var mandatory: Boolean = false
    var defaultValue: Type? = null
    var dataBinding: DataBinding<Type> = NoDataBinding
    val additionalQualifiers = mutableListOf<ParameterQualifier>()
    var validWhen: ValidationCondition = AlwaysValid

    fun validationActionCallTo(actionName: String) =
        actionCallTo(actionName).parameter(parameterName)

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
        validWhen.let {
            when (it) {
                AlwaysValid -> Unit
                else -> add(validationConditionQualifier(it))
            }
        }
    }

    operator fun MutableList<ParameterQualifier>.plusAssign(constraints: List<Constraint>) {
        this.addAll(constraints.map { it.toQualifier() })
    }

    protected open val customQualifiers: MutableList<ParameterQualifier> get() =
        ArrayList()
}

abstract class PrimitiveParameterBuilder<Type: Any>(parameterName: String, type: ParameterType<Type>) : BasicParameterBuilder<Type>(parameterName, type) {
    var predefinedAnswers: List<Type>? = null
    var predefinedAnswersFrom: ActionCallBuilder? = null
        set(value) {
            field = value?.snapshot()
        }

    override val customQualifiers get() = super.customQualifiers.apply {
        predefinedAnswers?.let {
            add(predefinedAnswersQualifier(type, it))
        }
        predefinedAnswersFrom?.let {
            add(predefinedAnswersActionQualifier(type, it.create()))
        }
    }
}

abstract class ArrayParameterBuilder<Type: Any>(name: String, type: array<Type>) : BasicParameterBuilder<List<Type>>(name, type) {
    var sameValues: Boolean? = null

    override val customQualifiers get() = super.customQualifiers.apply {
        sameValues?.let {
            add(sameValuesQualifier(it))
        }
    }
}

class ArrayPairParameterBuilder(name: String, type: array<Pair<String, String>>) :
    ArrayParameterBuilder<Pair<String, String>>(name, type)

class ArrayPrimitiveParameterBuilder<Type: Any>(name: String, type: array<Type>) : ArrayParameterBuilder<Type>(name, type) {
    var predefinedAnswers: List<Type>? = null
    var predefinedAnswersFrom: ActionCallBuilder? = null
        set(value) {
            field = value?.snapshot()
        }

    override val customQualifiers get() = super.customQualifiers.apply {
        predefinedAnswers?.let {
            add(predefinedAnswersQualifier(type, it))
        }
        predefinedAnswersFrom?.let {
            add(predefinedAnswersActionQualifier(type, it.create()))
        }
    }
}

class BooleanParameterBuilder(name: String) : PrimitiveParameterBuilder<Boolean>(name, boolean)

class IntParameterBuilder(name: String) : PrimitiveParameterBuilder<Long>(name, number) {
    var min: Long? = null
    var max: Long? = null

    override val customQualifiers get() = super.customQualifiers.apply {
        add(numberFormatQualifier("#0"))
        min?.let {
            add(minNumberValueQualifier(it))
        }
        max?.let {
            add(maxNumberValueQualifier(it))
        }
    }
}

class StringParameterBuilder(name: String) : PrimitiveParameterBuilder<String>(name, string) {
    var multiline: Boolean = false

    override val customQualifiers get() = super.customQualifiers.apply {
        if (multiline) {
            add(multilineQualifier())
        }
    }
}

class SecureStringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, SecureString)

class DateParameterBuilder(name: String) : PrimitiveParameterBuilder<Date>(name, date)

class ReferenceParameterBuilder(name: String, type: Reference) : BasicParameterBuilder<Reference>(name, type) {

    var showInInventory: Boolean = false
    var listedBy: ActionCallBuilder? = null
        set(value) {
            field = value?.snapshot()
        }
    var selector: ReferenceSelector = ReferenceSelector.tree
    var browserRoot: InventoryBrowserRoot = DefaultBrowserRoot

    override val customQualifiers get() = super.customQualifiers.apply {
        if (showInInventory) add(showInInventoryQualifier)
        listedBy?.let {
            add(listFromAction(it.create(), type))
        }
        add(selectWith(selector))
        browserRoot.ognl?.let { add(displayParentFrom(it)) }
    }
}

class ReferenceArrayParameterBuilder(name: String, type: array<Reference>) : ArrayParameterBuilder<Reference>(name, type)

class OutputParameterBuilder(val name: String, val type: ParameterType<Any>) {
    var description: String? = null

    fun buildOutputParameter() =
        ParameterInfo(name, type, description = description)
}

class AttributeBuilder(val name: String, val type: ParameterType<Any>) {
    var description: String? = null
    var readOnly: Boolean = false

    fun buildAttribute() =
        AttributeDefinition(name, type, description, readOnly)
}