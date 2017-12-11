/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ObjectReference
import java.lang.reflect.ParameterizedType

class Wrapper(
    val property: String,
    unwrapped: Class<*>,
    parent: Class<*>,
    simpleProperties: List<Property>,
    listProperties: List<Property>,
    rootClass: Class<*>,
    getterChain: List<String>
) : ClassProperties(simpleProperties, listProperties) {
    // val name = parent.wrapperName(property)
    val name = rootClass.simpleName + "_" + getterChain.joinToString("_")
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
    Wrapper(property, unwrapped, parent, simpleProperties, listProperties, Object::class.java, listOf())

private fun ClassProperties.allProperties(): Sequence<Property> =
    simpleProperties.asSequence() + listProperties.asSequence()

private fun Pair<Class<*>, Property>.toWrapper(classToProperties: Map<Class<*>, ClassProperties>): Wrapper {
    val parent = first
    val property = second

    val propertyClassProperties = classToProperties[property.clazz]!!
    return propertyClassProperties.toWrapper(property.propertyName, property.clazz, parent)
}

fun generateWrappersModel(nestedClasses: List<Class<*>>, relationsModel: RelationsModel): WrappersModel {

    val classToProperties: Map<Class<*>, ClassProperties> = nestedClasses.associateBy({ it }, { it.properties })
    val wrappers1 = classToProperties.asSequence()
        .map { (c, p) -> generateSequence { c }.zip(p.allProperties()) }
        .flatten()
        .filter { it.second.clazz.isApiClass }
        .map { it.toWrapper(classToProperties) }
        .toList()

    val nestedRelations = relationsModel.nestedRelations
    val wrappers2 = nestedRelations.map { relation ->
        val wrapperName = relation.rootClass.simpleName + "_" + relation.getterChain.joinToString("_")
        val newSimpleProperties = relation.simpleProperties.map { property ->
            val fieldName = property.fieldName.capitalize()
            val clazz = property.clazz

            val properWrapName = if (clazz.isApiClass) {
                wrapperName + "_" + fieldName
            } else {
                clazz.kotlinClassName
            }
            Property(property.fieldName, property.clazz, property.parent, properWrapName)
        }
        val newListProperties = relation.listProperties.map { property ->
            val fieldName = property.fieldName.capitalize()
            val clazz = property.clazz

            val properWrapName = if (clazz.isApiClass) {
                wrapperName + "_" + fieldName
            } else {
                clazz.kotlinClassName
            }
            Property(property.fieldName, property.clazz, property.parent, properWrapName)
        }

        Wrapper(
            relation.getterDecapitalized,
            relation.child,
            relation.parent,
            newSimpleProperties,
            newListProperties,
            relation.rootClass,
            relation.getterChain
        )
    }

    println("#DUPATEST : ")
    println("#DUPATEST : ${wrappers1.size}")
    println("#DUPATEST : ${wrappers2.size}")
    println("#DUPATEST : ")
    println("#DUPATEST : ")

    return WrappersModel(wrappers2)
}