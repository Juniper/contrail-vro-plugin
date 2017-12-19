/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.util

import com.google.common.reflect.ClassPath
import net.juniper.contrail.api.ApiObjectBase
import java.lang.reflect.Method
import java.lang.reflect.Modifier

val <T> Class<T>.nestedName get() =
    canonicalName.replace("${`package`?.name}.", "")

val <T> Class<T>.collapsedNestedName get() =
    nestedName.replace(".", "")

val <T> Class<T>.underscoredNestedName get() =
    nestedName.replace(".", "_")

fun <T> Class<T>.wrapperName(property: String) =
    "${underscoredNestedName}_${property.capitalize()}"

val <T> Class<T>.kotlinClassName: String get() = when (this) {
    java.lang.Integer::class.java, java.lang.Integer.TYPE -> "Int"
    java.lang.Boolean.TYPE -> "Boolean"
    else -> simpleName
}

private val String.ref get() =
    "${this}Ref"

private val getterPattern = "^get".toRegex()
private val backRefsPattern = "BackRefs$".toRegex()

val Method.nameWithoutGet get() =
    name.replace(getterPattern, "")

val Method.nameWithoutGetAndBackRefs get() =
    nameWithoutGet.replace(backRefsPattern, "")

val Method.propertyName get() =
    nameWithoutGet.decapitalize()

val Method.isGetter get() =
    name.startsWith("get")

val Method.referenceName get() =
    nameWithoutGetAndBackRefs.ref

val <T> Class<T>.referenceName: String get() =
    simpleName.ref

val <T> Class<T>.isAbstract: Boolean get() =
    Modifier.isAbstract(modifiers)

val <T> Class<T>.isNotAbstract: Boolean get() =
    !isAbstract

private val loader get(): ClassLoader =
    Thread.currentThread().contextClassLoader

fun <T> Class<T>.nonAbstractSubclassesIn(packageName: String): List<Class<out T>> {
    val classes = subclassesIn(packageName)
    @Suppress("UNCHECKED_CAST")
    return classes.map { it as Class<out T> }
        .filter { it.isNotAbstract }
}

fun <T> Class<T>.nonAbstractSubclasses(): List<Class<out T>> =
    nonAbstractSubclassesIn(`package`.name)

private fun <T> Class<T>.subclassesIn(packageName: String): List<Class<*>> =
    classesIn(packageName)
        .filter { it.superclass == this }
        .toList()

private fun classesIn(packageName: String): Sequence<Class<*>> =
    ClassPath.from(loader).getTopLevelClassesRecursive(packageName).asSequence()
        .map { classForName(it.name) }
        .filterNotNull()

fun classForName(name: String): Class<*>? =
    try { Class.forName(name) } catch (e: ClassNotFoundException) { null }

val Class<*>.xsdName : String
    get() = simpleName.splitCamel().toLowerCase().replace(" ", "-")

val <T> Class<T>.xsdType: String get() = when (this.simpleName) {
    "String" -> "string"
    "Boolean" -> "boolean"
    "Int" -> "number"
    else -> ""
}

val Class<out ApiObjectBase>.hasParent
    get() = this.newInstance().defaultParentType != null