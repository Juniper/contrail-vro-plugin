/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isGetter
import net.juniper.contrail.vro.config.kotlinClassName
import net.juniper.contrail.vro.config.propertyName
import net.juniper.contrail.vro.config.returnListGenericClass
import net.juniper.contrail.vro.config.toPluginMethodName
import net.juniper.contrail.vro.config.underscoredNestedName
import net.juniper.contrail.vro.config.underscoredPropertyToCamelCase
import net.juniper.contrail.vro.config.wrapperName
import java.lang.reflect.Method

class Property(
    val propertyName: String,
    val clazz: Class<*>,
    val parent: Class<*>,
    val declaringClass: Class<*>,
    val isList: Boolean,
    val wrapname: String? = null)
{
    val pluginPropertyName = propertyName.capitalize().toPluginMethodName.decapitalize()
    val componentName get() = propertyName.replace("List$".toRegex(), "").capitalize()
    val classLabel get() = if (clazz.isApiTypeClass) clazz.underscoredNestedName else clazz.kotlinClassName
    val wrapperName get() = wrapname ?: if (clazz.isApiTypeClass) parent.wrapperName(propertyName) else clazz.kotlinClassName
}

open class ClassProperties(
    val properties: List<Property>
) {
    val simpleProperties: List<Property> = properties.filter { ! it.isList }
    val listProperties: List<Property> = properties.filter { it.isList }
}

val <T> Class<T>.properties: ClassProperties get() =
    declaredMethods.asSequence()
        .filter { it.isGetter }
        .map { it.toPropertyOf(this) }
        .filterNotNull()
        .toClassProperties()

private fun Sequence<Property>.toClassProperties() =
    ClassProperties(toList())

private fun Method.toPropertyOf(parent: Class<*>): Property? {
    val type = returnType
    val propertyName = propertyName
    return if (type == List::class.java) {
        val genericArg = returnListGenericClass ?: return null
        Property(propertyName, genericArg, parent, declaringClass, true)
    } else {
        Property(propertyName, type, parent, declaringClass, false)
    }
}