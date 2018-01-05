/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import net.juniper.contrail.vro.generator.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.generator.workflows.model.ParameterType
import net.juniper.contrail.vro.generator.workflows.model.PresentationGroup
import net.juniper.contrail.vro.generator.workflows.model.PresentationStep
import net.juniper.contrail.vro.generator.workflows.model.Reference
import net.juniper.contrail.vro.generator.workflows.model.SecureString
import net.juniper.contrail.vro.generator.workflows.model.boolean
import net.juniper.contrail.vro.generator.workflows.model.defaultValueQualifier
import net.juniper.contrail.vro.generator.workflows.model.mandatoryQualifier
import net.juniper.contrail.vro.generator.workflows.model.maxNumberValueQualifier
import net.juniper.contrail.vro.generator.workflows.model.minNumberValueQualifier
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.numberFormatQualifier
import net.juniper.contrail.vro.generator.workflows.model.showInInventoryQualifier
import net.juniper.contrail.vro.generator.workflows.model.string

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
        steps += PresentationStep.fromParameters(title, stepParameters.asPresentationParameters, aggregator.description)
    }

    fun groups(title: String, setup: PresentationGroupBuilder.() -> Unit) {
        val stepGroups = mutableListOf<PresentationGroup>()
        val groupBuilder = PresentationGroupBuilder(stepGroups, allParameters)
        groupBuilder.setup()
        steps += PresentationStep.fromGroups(title, stepGroups, groupBuilder.description)
    }
}

@WorkflowBuilder
class PresentationGroupBuilder(
    private val groups: MutableList<PresentationGroup>,
    private val allParameters: MutableList<ParameterInfo>
) {
    var description: String? = null

    fun group(title: String, setup: ParameterAggregator.() -> Unit) {
        val groupParameters = mutableListOf<ParameterInfo>()
        val aggregator = ParameterAggregator(groupParameters, allParameters)
        aggregator.setup()
        groups += PresentationGroup(title, groupParameters.asPresentationParameters, aggregator.description)
    }
}

@WorkflowBuilder
@Suppress("UNUSED_PARAMETER")
open class ParameterAggregator(
    private val parameters: MutableList<ParameterInfo>,
    protected val allParameters: MutableList<ParameterInfo>
) {
    var description: String? = null

    fun parameter(name: String, type: boolean, setup: BooleanParameterBuilder.() -> Unit) =
        BooleanParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: number, setup: IntParameterBuilder.() -> Unit) =
        IntParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: string, setup: StringParameterBuilder.() -> Unit) =
        StringParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: SecureString, setup: SecureStringParameterBuilder.() -> Unit) =
        SecureStringParameterBuilder(name).updateWith(setup)

    fun parameter(name: String, type: Reference, setup: ReferenceParameterBuilder.() -> Unit) {
        ReferenceParameterBuilder(name, type).updateWith(setup)
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

    val parameterInfo get() = ParameterInfo(
        name = name,
        type = type,
        description = description,
        qualifiers = allQualifiers
    )

    private val allQualifiers get(): List<ParameterQualifier> =
        commonQualifiers + customQualifiers

    private val commonQualifiers get() = mutableListOf<ParameterQualifier>().apply {
        if (mandatory) add(mandatoryQualifier)

        defaultValue?.let {
            add(defaultValueQualifier(type, it))
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

class ReferenceParameterBuilder(name: String, type: Reference) : BasicParameterBuilder<Reference>(name, type) {

    var showInInventory: Boolean = false

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        if (showInInventory) qualifiers.add(showInInventoryQualifier)
        return qualifiers
    }
}
