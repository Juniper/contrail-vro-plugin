/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import net.juniper.contrail.vro.generator.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.generator.workflows.model.ParameterType
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

class PresentationParametersBuilder(
    private val steps: MutableList<PresentationStep>,
    parameters: MutableList<ParameterInfo>
) : ParameterAggregator(parameters) {

    fun step(title: String, setup: ParameterAggregator.() -> Unit) {
        val stepParameters = mutableListOf<ParameterInfo>()
        ParameterAggregator(stepParameters).apply(setup).apply {
            steps.add(PresentationStep(title, stepParameters.asPresentationParameters))
        }
    }
}

@Suppress("UNUSED_PARAMETER")
open class ParameterAggregator(
    protected val parameters: MutableList<ParameterInfo>
) {
    @JvmName("booleanParameter")
    fun parameter(name: String, type: boolean, setup: BooleanParameterBuilder.() -> Unit) =
        BooleanParameterBuilder(name).updateWith(setup)

    @JvmName("intParameter")
    fun parameter(name: String, type: number, setup: IntParameterBuilder.() -> Unit) =
        IntParameterBuilder(name).updateWith(setup)

    @JvmName("stringParameter")
    fun parameter(name: String, type: string, setup: StringParameterBuilder.() -> Unit) =
        StringParameterBuilder(name).updateWith(setup)

    @JvmName("secureStringParameter")
    fun parameter(name: String, type: SecureString, setup: SecureStringParameterBuilder.() -> Unit) =
        SecureStringParameterBuilder(name).updateWith(setup)

    @JvmName("referenceParameter")
    fun parameter(name: String, type: Reference, setup: ReferenceParameterBuilder.() -> Unit) {
        ReferenceParameterBuilder(name, type).updateWith(setup)
    }

    private fun <Builder : BasicParameterBuilder<T>, T : Any> Builder.updateWith(setup: Builder.() -> Unit) =
        apply(setup).append()

    private fun BasicParameterBuilder<*>.append() {
        parameters.add(parameterInfo())
    }
}

abstract class BasicParameterBuilder<Type: Any>(val name: String, val type: ParameterType<Type>) {
    var description: String = name.capitalize()
    var mandatory: Boolean = false
    var defaultValue: Type? = null

    fun parameterInfo() = ParameterInfo(
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
