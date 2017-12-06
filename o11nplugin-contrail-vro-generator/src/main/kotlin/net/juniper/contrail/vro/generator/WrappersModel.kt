/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ObjectReference
import java.lang.reflect.ParameterizedType

class Wrapper(
    property: String,
    unwrapped: Class<*>,
    parent: Class<*>,
    simpleProperties: List<Property>,
    listProperties: List<Property>
) : ClassProperties(simpleProperties, listProperties) {
    val name = parent.wrapperName(property)
    val unwrappedName = unwrapped.nestedName
    val unwrappedLabel = unwrapped.underscoredNestedName
    val parentName = parent.nestedName
}

class WrappersModel(
    val wrappers: List<Wrapper>)
    : GenericModel()

private val <T> Class<T>.properties: ClassProperties
    get() {
        val simpleProperties = mutableListOf<Property>()
        val listProperties = mutableListOf<Property>()

        for (field in declaredFields) {
            val type = field.type
            val fieldName = field.name
            if (type == java.util.List::class.java) {
                val genericType = field.genericType as ParameterizedType
                val genericArg = genericType.actualTypeArguments[0] as Class<*>
                if (genericArg != ObjectReference::class.java)
                    listProperties.add(Property(fieldName, genericArg, this))
            } else {
                simpleProperties.add(Property(fieldName, type, this))
            }
        }

        return ClassProperties(simpleProperties, listProperties)
    }

private fun ClassProperties.toWrapper(property: String, unwrapped: Class<*>, parent: Class<*>): Wrapper =
    Wrapper(property, unwrapped, parent, simpleProperties, listProperties)

private fun ClassProperties.allProperties(): Sequence<Property> =
    simpleProperties.asSequence() + listProperties.asSequence()

private fun Pair<Class<*>, Property>.toWrapper(classToProperties: Map<Class<*>, ClassProperties>): Wrapper {
    val parent = first
    val property = second

    val propertyClassProperties = classToProperties[property.clazz]!!
    return propertyClassProperties.toWrapper(property.propertyName, property.clazz, parent)
}

fun generateWrappersModel(nestedClasses: List<Class<*>>): WrappersModel {
    val classToProperties: Map<Class<*>, ClassProperties> = nestedClasses.associateBy({ it }, { it.properties })
    val wrappers = classToProperties.asSequence()
        .map { (c, p) -> generateSequence { c }.zip(p.allProperties()) }
        .flatten()
        .filter { it.second.clazz.isApiClass }
        .map { it.toWrapper(classToProperties) }
        .toList()

    return WrappersModel(wrappers)
}