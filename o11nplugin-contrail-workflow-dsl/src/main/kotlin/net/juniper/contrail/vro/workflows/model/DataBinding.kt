/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

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

class FromListPropertyValue<Type: Any>(
    val parentItem: String,
    val childItem: String,
    val listAccessor: String,
    val propertyPath: String,
    val type: ParameterType<Type>
) : DataBinding<Type>() {
    override val qualifier: ParameterQualifier? get() =
        bindValueToListProperty(
            parentItem = parentItem,
            childItem = childItem,
            listAccessor = listAccessor,
            propertyPath = propertyPath,
            type = type
        )
}

class FromAction<Type : Any>(val actionName: String, val type: ParameterType<Type>, vararg val parameters: String) : DataBinding<Type>() {
    override val qualifier: ParameterQualifier? get() =
        bindValueToAction(actionName, type, *parameters)
}