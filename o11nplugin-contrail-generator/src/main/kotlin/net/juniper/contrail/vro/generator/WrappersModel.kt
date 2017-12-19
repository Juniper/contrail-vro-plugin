/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

data class WrappersModel(
    val references: List<ReferenceWrapperModel>,
    val wrappers: List<WrapperModel>
) : GenericModel()

data class ReferenceWrapperModel(
    val className: String,
    val referenceName: String
)

data class WrapperModel(
    val name: String,
    val property: String,
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

fun Property.toPropertyModel() =
    PropertyModel(
        propertyName,
        wrapperName,
        componentName,
        classLabel
    )

fun ReferenceWrapper.toReferenceWrapperModel() =
    ReferenceWrapperModel(
        simpleName,
        referenceName
    )

class ReferenceWrapper(
    val simpleName: String,
    val referenceName: String
)

fun Class<*>.toReferenceWrapper() =
    ReferenceWrapper(
        simpleName,
        referenceName
    )

private fun wrapperName(rootClass: Class<*>, getterChain: List<Getter>) =
    rootClass.simpleName + getterChain.joinToString("") { "_" + it.name }

private fun Property.toWrapperProperties(wrapperName: String) : Property {
    val fieldName = fieldName.capitalize()

    val wrapName = if (clazz.isApiTypeClass)
        "${wrapperName}_$fieldName"
    else
        clazz.kotlinClassName

    return Property(propertyName, clazz, parent, wrapName)
}

private fun List<Property>.toWrapperProperties(wrapperName: String) : List<Property> =
    map { it.toWrapperProperties(wrapperName) }

private fun NestedRelation.toWrapperModel() : WrapperModel {
    val wrapperName = wrapperName(rootClass, getterChain)
    val newSimpleProperties = simpleProperties.toWrapperProperties(wrapperName)
    val newListProperties = listProperties.toWrapperProperties(wrapperName)

    val name = wrapperName(rootClass, getterChain)
    val unwrappedName = child.nestedName
    val unwrappedLabel = child.underscoredNestedName
    return WrapperModel(
        name,
        getterDecapitalized,
        newSimpleProperties.map { it.toPropertyModel() },
        newListProperties.map { it.toPropertyModel() },
        unwrappedName,
        unwrappedLabel
    )
}

fun generateReferenceWrappers(objectClasses: List<Class<out ApiObjectBase>>) =
    objectClasses.map { it.toReferenceWrapper() }

fun generateWrappersModel(referenceWrappers: List<ReferenceWrapper>, nestedRelations: List<NestedRelation>): WrappersModel {
    val references = referenceWrappers.map { it.toReferenceWrapperModel() }

    val wrappers = nestedRelations
        .map { it.toWrapperModel() }

    return WrappersModel(references, wrappers)
}