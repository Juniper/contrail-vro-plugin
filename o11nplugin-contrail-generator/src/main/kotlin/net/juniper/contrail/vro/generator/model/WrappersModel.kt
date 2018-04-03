/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isPropertyListWrapper
import net.juniper.contrail.vro.config.kotlinClassName
import net.juniper.contrail.vro.config.nestedName
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.underscoredNestedName

data class WrappersModel(
    val wrappers: List<WrapperModel>
) : GenericModel()

data class WrapperModel(
    val name: String,
    val property: String,
    val displayName: String,
    val simpleProperties: List<PropertyModel>,
    val listProperties: List<PropertyModel>,
    val unwrappedName: String,
    val unwrappedLabel: String
)

data class PropertyModel(
    val propertyName: String,
    val wrapperName: String,
    val componentName: String,
    val classLabel: String
)

fun Property.toPropertyModel() = PropertyModel(
    propertyName,
    wrapperName,
    componentName,
    classLabel
)

fun wrapperName(rootClass: Class<*>, getterChain: List<Getter>) =
    rootClass.pluginName + getterChain.joinToString("") { "_" + it.name }

private fun Property.toWrapperProperties(wrapperName: String) : Property {
    val fieldName = fieldName.capitalize()

    val wrapName = if (clazz.isApiTypeClass)
        "${wrapperName}_$fieldName"
    else
        clazz.kotlinClassName

    return Property(propertyName, clazz, parent, declaringClass, isList, wrapName)
}

private fun List<Property>.toWrapperProperties(wrapperName: String) : List<Property> =
    map { it.toWrapperProperties(wrapperName) }

private fun NestedRelation.toWrapperModel() : WrapperModel {
    val wrapperName = wrapperName(rootClass, getterChain)
    val newSimpleProperties = simpleProperties.filter { !it.clazz.isPropertyListWrapper }.toWrapperProperties(wrapperName)
    val newListProperties = listProperties.toWrapperProperties(wrapperName)

    val name = wrapperName(rootClass, getterChain)
    val unwrappedName = child.nestedName
    val unwrappedLabel = child.underscoredNestedName
    return WrapperModel(
        name,
        getterDecapitalized,
        folderName,
        newSimpleProperties.map { it.toPropertyModel() },
        newListProperties.map { it.toPropertyModel() },
        unwrappedName,
        unwrappedLabel
    )
}

fun generateWrappersModel(nestedRelations: List<NestedRelation>): WrappersModel {

    val wrappers = nestedRelations
        .map { it.toWrapperModel() }

    return WrappersModel(wrappers)
}