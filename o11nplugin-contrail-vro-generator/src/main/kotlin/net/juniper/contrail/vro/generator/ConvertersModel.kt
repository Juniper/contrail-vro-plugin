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
    val classLabel get() = if (clazz.isApiClass) clazz.underscoredNestedName else clazz.kotlinClassName
    val wrapperName get() = wrapname ?: if (clazz.isApiClass) parent.wrapperName(propertyName) else clazz.kotlinClassName
}

val Property.isApiProperty get() =
    clazz.isApiClass

open class ClassProperties(
    val simpleProperties: List<Property>,
    val listProperties: List<Property>
) {

    val isEmpty: Boolean =
        simpleProperties.isEmpty() && listProperties.isEmpty()
    val isNotEmpty: Boolean get() =
        ! isEmpty
}

class Proxy(
    val name: String,
    simpleProperties: List<Property>,
    listProperties: List<Property>
) : ClassProperties(simpleProperties, listProperties)

class Converter(
    targetClass: Class<*>,
    simpleProperties: List<Property>,
    listProperties: List<Property>
) : ClassProperties(simpleProperties, listProperties) {
    val proxyName = targetClass.simpleName
    val targetName = targetClass.canonicalName
    val targetCollapsedName = targetClass.collapsedNestedName
}

class ConvertersModel(
    val proxies: List<Proxy>,
    val converters: List<Converter>
) : GenericModel() {
    private val proxyNames = proxies.asSequence().map { it.name }.toSet()

    fun isProxy(name: String) =
        proxyNames.contains(name)
}

fun generateConvertersModel(aliases: AliasClasses, propertyClasses: List<Class<*>>) : ConvertersModel {
    val proxies = aliases.asMap().entries
        .map { (key, value) -> toProxy(key, value) }
    val converters = aliases.values()
        .map { it.converter() }

    return ConvertersModel(proxies, converters)
}

private fun toProxy(name: String, aliases: Collection<Class<*>>) : Proxy {
    val subject = aliases.asSequence().sortedBy { it.declaredFields.size }.last()

    val properties = subject.properties

    return Proxy(name, properties.simpleProperties, properties.listProperties)
}

private fun <T> Class<T>.converter(): Converter {
    val props = properties
    return Converter(this, props.simpleProperties, props.listProperties)
}

private val <T> Class<T>.properties: ClassProperties get() {
    val simpleProperties = mutableListOf<Property>()
    val listProperties = mutableListOf<Property>()

    for (field in declaredFields) {
        val type = field.type
        val fieldName = field.name
        if (type == java.util.List::class.java) {
            val genericType = field.genericType as ParameterizedType
            val genericArg = genericType.actualTypeArguments[0] as Class<*>
            listProperties.add(Property(fieldName, genericArg, this))
        } else {
            simpleProperties.add(Property(fieldName, type, this))
        }
    }

    return ClassProperties(simpleProperties, listProperties)
}