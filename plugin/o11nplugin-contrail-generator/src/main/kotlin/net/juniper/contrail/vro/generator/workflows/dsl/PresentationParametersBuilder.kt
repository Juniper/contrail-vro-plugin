/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import net.juniper.contrail.vro.generator.workflows.model.Action
import net.juniper.contrail.vro.generator.workflows.model.BooleanVisibilityCondition
import net.juniper.contrail.vro.generator.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.generator.workflows.model.ParameterType
import net.juniper.contrail.vro.generator.workflows.model.PresentationGroup
import net.juniper.contrail.vro.generator.workflows.model.PresentationStep
import net.juniper.contrail.vro.generator.workflows.model.Reference
import net.juniper.contrail.vro.generator.workflows.model.SecureString
import net.juniper.contrail.vro.generator.workflows.model.StringVisibilityCondition
import net.juniper.contrail.vro.generator.workflows.model.VisibilityCondition
import net.juniper.contrail.vro.generator.workflows.model.array
import net.juniper.contrail.vro.generator.workflows.model.boolean
import net.juniper.contrail.vro.generator.workflows.model.date
import net.juniper.contrail.vro.generator.workflows.model.defaultValueQualifier
import net.juniper.contrail.vro.generator.workflows.model.listFromAction
import net.juniper.contrail.vro.generator.workflows.model.mandatoryQualifier
import net.juniper.contrail.vro.generator.workflows.model.maxNumberValueQualifier
import net.juniper.contrail.vro.generator.workflows.model.minNumberValueQualifier
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.numberFormatQualifier
import net.juniper.contrail.vro.generator.workflows.model.predefinedAnswersQualifier
import net.juniper.contrail.vro.generator.workflows.model.selectAsTreeQualifier
import net.juniper.contrail.vro.generator.workflows.model.showInInventoryQualifier
import net.juniper.contrail.vro.generator.workflows.model.string
import net.juniper.contrail.vro.generator.workflows.model.visibleWhenBooleanSwitchedQualifier
import net.juniper.contrail.vro.generator.workflows.model.visibleWhenNonNullQualifier
import net.juniper.contrail.vro.generator.workflows.model.visibleWhenVariableHasValueQualifier
import java.util.Date

@WorkflowBuilder
class PresentationParametersBuilder(
    private val steps: MutableList<PresentationStep>,
    parameters: MutableList<ParameterInfo>,
    allParameters: MutableList<ParameterInfo>
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
}

@WorkflowBuilder
class PresentationGroupBuilder(
    private val groups: MutableList<PresentationGroup>,
    private val allParameters: MutableList<ParameterInfo>
) {
    var description: String? = null
    var visible: VisibilityCondition? = null

    fun group(title: String, setup: ParameterAggregator.() -> Unit) {
        val groupParameters = mutableListOf<ParameterInfo>()
        val aggregator = ParameterAggregator(groupParameters, allParameters)
        aggregator.setup()
        groups += PresentationGroup(title, groupParameters.asPresentationParameters, aggregator.description, aggregator.qualifiers)
    }

    val qualifiers get() = mutableListOf<ParameterQualifier>().apply {
        visible?.let {
            when (it) {
                is BooleanVisibilityCondition -> add(visibleWhenBooleanSwitchedQualifier(it.name))
                is StringVisibilityCondition -> add(visibleWhenVariableHasValueQualifier(it.name, it.value))
            }
        }
    }
}

@WorkflowBuilder
@Suppress("UNUSED_PARAMETER")
open class ParameterAggregator(
    private val parameters: MutableList<ParameterInfo>,
    protected val allParameters: MutableList<ParameterInfo>
) {
    var description: String? = null
    var visible: VisibilityCondition? = null

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

    val qualifiers get() = mutableListOf<ParameterQualifier>().apply {
        visible?.let {
            when (it) {
                is BooleanVisibilityCondition -> add(visibleWhenBooleanSwitchedQualifier(it.name))
                is StringVisibilityCondition -> add(visibleWhenVariableHasValueQualifier(it.name, it.value))
            }
        }
    }

    private fun <Builder : BasicParameterBuilder<T>, T : Any> Builder.updateWith(setup: Builder.() -> Unit) =
        apply(setup).appendParameterLists()

    private fun BasicParameterBuilder<*>.appendParameterLists() {
        parameterInfo.also {
            parameters.add(it)
            allParameters.add(it)
        }
    }
}

abstract class BasicParameterBuilder<Type: Any>(val name: String, val type: ParameterType<Type>) {
    var description: String = name.capitalize()
    var mandatory: Boolean = false
    var defaultValue: Type? = null
    var predefinedAnswers: List<Type>? = null
    private var dependsOn: String? = null
    private var listedBy: Action? = null
    var visible: VisibilityCondition? = null

    val parameterInfo get() = ParameterInfo(
        name = name,
        type = type,
        description = description,
        qualifiers = allQualifiers
    )

    fun listedBy(action: Action) {
        listedBy = action
    }

    fun dependsOn(parameter: String) {
        dependsOn = parameter
    }

    private val allQualifiers get(): List<ParameterQualifier> =
        commonQualifiers + customQualifiers

    private val commonQualifiers get() = mutableListOf<ParameterQualifier>().apply {
        if (mandatory) add(mandatoryQualifier)

        defaultValue?.let {
            add(defaultValueQualifier(type, it))
        }
        predefinedAnswers?.let {
            add(predefinedAnswersQualifier(type, it))
        }
        dependsOn?.let {
            add(visibleWhenNonNullQualifier(it))
        }
        listedBy?.let {
            add(listFromAction(it))
        }
        visible?.let {
            when (it) {
                is BooleanVisibilityCondition -> add(visibleWhenBooleanSwitchedQualifier(it.name))
                is StringVisibilityCondition -> add(visibleWhenVariableHasValueQualifier(it.name, it.value))
            }
        }
    }

    protected open val customQualifiers: List<ParameterQualifier> get() =
        emptyList()
}

class BooleanParameterBuilder(name: String) : BasicParameterBuilder<Boolean>(name, boolean)

class IntParameterBuilder(name: String) : BasicParameterBuilder<Int>(name, number) {
    var min: Int? = null
    var max: Int? = null

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

class StringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, string)

class SecureStringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, SecureString)

class DateParameterBuilder(name: String) : BasicParameterBuilder<Date>(name, date)

class ReferenceParameterBuilder(name: String, type: Reference) : BasicParameterBuilder<Reference>(name, type) {

    var showInInventory: Boolean = false

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        qualifiers.add(selectAsTreeQualifier)
        if (showInInventory) qualifiers.add(showInInventoryQualifier)
        return qualifiers
    }
}

class ReferenceArrayParameterBuilder(name: String, type: array<Reference>) : BasicParameterBuilder<List<Reference>>(name, type)