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

class Wrapper(
    val property: String,
    unwrapped: Class<*>,
    simpleProperties: List<Property>,
    listProperties: List<Property>,
    rootClass: Class<*>,
    getterChain: List<Getter>
) : ClassProperties(simpleProperties, listProperties) {
    val name = wrapperName(rootClass, getterChain)
    val unwrappedName = unwrapped.nestedName
    val unwrappedLabel = unwrapped.underscoredNestedName
}

class ReferenceWrapper(
    val simpleName: String,
    val referenceName: String
)

fun Class<*>.toReferenceWrapper() =
    ReferenceWrapper(
        simpleName,
        referenceName
    )

fun Wrapper.toWrapperModel() =
    WrapperModel(
        name,
        property,
        simpleProperties.map { it.toPropertyModel() },
        listProperties.map { it.toPropertyModel() },
        unwrappedName,
        unwrappedLabel
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

private fun NestedRelation.toWrapper() : Wrapper {
    val wrapperName = wrapperName(rootClass, getterChain)
    val newSimpleProperties = simpleProperties.toWrapperProperties(wrapperName)
    val newListProperties = listProperties.toWrapperProperties(wrapperName)

    return Wrapper(
        getterDecapitalized,
        child,
        newSimpleProperties,
        newListProperties,
        rootClass,
        getterChain
    )
}

fun generateReferenceWrappers(objectClasses: List<Class<out ApiObjectBase>>) =
    objectClasses.map { it.toReferenceWrapper() }

fun generateWrappersModel(referenceWrappers: List<ReferenceWrapper>, nestedRelations: List<NestedRelation>): WrappersModel {
    val references = referenceWrappers.map { it.toReferenceWrapperModel() }

    val wrappers = nestedRelations
        .map { it.toWrapper() }
        .map { it.toWrapperModel() }

    return WrappersModel(references, wrappers)
}