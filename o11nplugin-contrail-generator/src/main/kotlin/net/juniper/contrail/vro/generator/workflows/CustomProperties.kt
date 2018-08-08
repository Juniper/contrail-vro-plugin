/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.vro.config.constants.Contrail
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.readSubnet
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.custom.isSubnet

val Class<*>.hasCustomInput get() =
    customProperties.containsKey(this)

val customProperties = customize()
    .with(CustomSubnetType)
    .done()

interface CustomProperty<T : ApiPropertyBase> {
    fun Property.setup(builder: ParameterAggregator, schema: Schema, createMode: Boolean, propertyPath: () -> String)
    fun code(parameterName: String): String
}

private fun customize() =
    Builder()

private class Builder {
    private val properties = mutableMapOf<Class<out ApiPropertyBase>, CustomProperty<out ApiPropertyBase>>()

    inline fun <reified T : ApiPropertyBase> with(property: CustomProperty<T>) = apply {
        properties[T::class.java] = property
    }

    fun done() =
        properties.toMap()
}

private object CustomSubnetType : CustomProperty<SubnetType> {
    override fun Property.setup(builder: ParameterAggregator, schema: Schema, createMode: Boolean, propertyPath: () -> String) {
        builder.parameter(propertyName, string) {
            description = description(schema)
            validWhen = isSubnet()
            if (!createMode)
                dataBinding = fromAction(readSubnet, string) { parameter(item).string("${propertyPath().preparePrefix()}$propertyName") }
            additionalQualifiers += schema.simpleTypeConstraints(parent, propertyName, ignoreMissing = true)
        }
    }

    override fun code(parameterName: String): String =
        "${Contrail}Utils.parseSubnet($parameterName)"
}