/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.config.listElementProperty
import net.juniper.contrail.vro.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.any

abstract class DataBinding<in Type : Any> {
    abstract val qualifier: ParameterQualifier?
}

object NoDataBinding : DataBinding<Any>() {
    override val qualifier: ParameterQualifier? get() =
        null
}

class FromParameterValue(val parameter: String) : DataBinding<Any>() {
    override val qualifier: ParameterQualifier? get() =
        bindDataTo(parameter, any)
}

class NullStateOfProperty(val item: String, val propertyPath: String) : DataBinding<Boolean>() {
    override val qualifier: ParameterQualifier? get() =
        bindValueToNullableState(item, propertyPath)
}

class FromSimplePropertyValue<Type : Any>(
    val item: String,
    val property: String,
    val type: ParameterType<Type>
) : DataBinding<Type>() {
    override val qualifier: ParameterQualifier? get() =
        bindValueToSimpleProperty(item, property, type)
}

class FromComplexPropertyValue<Type : Any>(
    val item: String,
    val propertyPath: String,
    val type: ParameterType<Type>
) : DataBinding<Type>() {
    override val qualifier: ParameterQualifier? get() =
        bindValueToComplexProperty(item, propertyPath, type)
}

class FromAction<Type : Any>(val actionCall: ActionCall, val type: ParameterType<Type>) : DataBinding<Type>() {
    override val qualifier: ParameterQualifier? get() =
        bindValueToAction(actionCall, type)
}

fun <Type : Any> fromAction(actionName: String, type: ParameterType<Type>, setup: ActionCallBuilder.() -> Unit): DataBinding<Type> =
    FromAction(actionCallTo(actionName).apply(setup).create(), type)

fun <Type : Any> fromListElementProperty(
    parentItem: String,
    elementItem: String,
    propertyPrefix: String,
    propertyName: String,
    type: ParameterType<Type>
) = fromAction(listElementProperty, type) {
    parameter(parentItem)
    parameter(elementItem)
    string(propertyPrefix)
    string(propertyName.capitalize())
}