/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class ReferenceWrapper(clazz: Class<out ApiObjectBase>) {
    val className = clazz.simpleName
    val referenceName = clazz.referenceName
}

private fun Class<out ApiObjectBase>.toReferenceWrapper() =
    ReferenceWrapper(this)

class Wrapper(
    val property: String,
    unwrapped: Class<*>,
    parent: Class<*>,
    simpleProperties: List<Property>,
    listProperties: List<Property>,
    rootClass: Class<*>,
    getterChain: List<Getter>
) : ClassProperties(simpleProperties, listProperties) {
    val name = wrapperName(rootClass, getterChain)
    val unwrappedName = unwrapped.nestedName
    val unwrappedLabel = unwrapped.underscoredNestedName
    val parentName = parent.nestedName
}

class WrappersModel(
    val references: List<ReferenceWrapper>,
    val wrappers: List<Wrapper>
) : GenericModel()

private fun wrapperName(clazz: Class<*>, getterChain: List<Getter>) =
    clazz.simpleName + getterChain.joinToString("") { "_" + it.name }

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
        parent,
        newSimpleProperties,
        newListProperties,
        rootClass,
        getterChain
    )
}

fun generateWrappersModel(objectClasses: List<Class<out ApiObjectBase>>, relationsModel: RelationsModel): WrappersModel {

    val references = objectClasses
        .map { it.toReferenceWrapper() }

    val wrappers = relationsModel.nestedRelations
        .map { it.toWrapper() }

    return WrappersModel(references, wrappers)
}