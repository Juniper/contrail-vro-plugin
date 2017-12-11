/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import java.lang.reflect.ParameterizedType

class Property(val fieldName: String, val clazz: Class<*>, val parent: Class<*>, val wrapname: String? = null) {
    val className = clazz.kotlinClassName
    val collapsedName get() = clazz.collapsedNestedName
    val propertyName = fieldName.underscoredPropertyToCamelCase()
    val componentName get() = propertyName.replace("List$".toRegex(), "").capitalize()
    val classLabel get() = if (clazz.isApiTypeClass) clazz.underscoredNestedName else clazz.kotlinClassName
    val wrapperName get() = wrapname ?: if (clazz.isApiTypeClass) parent.wrapperName(propertyName) else clazz.kotlinClassName
}

open class ClassProperties(
    val simpleProperties: List<Property>,
    val listProperties: List<Property>
)

val <T> Class<T>.properties: ClassProperties get() {
    val simpleProperties = mutableListOf<Property>()
    val listProperties = mutableListOf<Property>()

    for (method in declaredMethods.filter { it.name.startsWith("get") }) {
        val type = method.returnType
        val propertyName = method.propertyName
        if (type == java.util.List::class.java) {
            val genericType = method.genericReturnType as ParameterizedType
            val genericArg = genericType.actualTypeArguments[0] as? Class<*> ?: continue
            listProperties.add(Property(propertyName, genericArg, this))
        } else {
            simpleProperties.add(Property(propertyName, type, this))
        }
    }

    return ClassProperties(simpleProperties, listProperties)
}