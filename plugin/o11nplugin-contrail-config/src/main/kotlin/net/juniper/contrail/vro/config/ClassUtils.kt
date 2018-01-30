/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import com.google.common.reflect.ClassPath
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

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

private val BackRefs = "BackRefs"
private val back_refs = "_back_refs"

private val getterPattern = "^get".toRegex()
private val backRefsPattern = "$BackRefs$".toRegex()
private val fieldBackRefsPattern = "$back_refs$".toRegex()

val Method.nameWithoutGet get() =
    name.replace(getterPattern, "")

val Method.nameWithoutGetAndBackRefs get() =
    nameWithoutGet.replace(backRefsPattern, "")

val Method.propertyName get() =
    nameWithoutGet.decapitalize()

val Method.isGetter get() =
    name.startsWith("get")

val Method.isBackRef get() =
    isGetter and name.endsWith(BackRefs)

val Field.isBackRef get() =
    name.endsWith(back_refs)

val Field.backRefTypeName get() =
    name.replace(fieldBackRefsPattern, "").fieldToClassName

val Method.referenceName get() =
    nameWithoutGetAndBackRefs.ref

val Type.parameterClass: Class<*>? get() =
    parameterType?.unwrapped

val Type.parameterType: Type? get() =
    if (this is ParameterizedType) actualTypeArguments[0] else null

val Type.unwrapped: Class<*>? get() =
    if (this is ParameterizedType) rawType as? Class<*> else this as? Class<*>

val Method.returnListGenericClass: Class<*>? get() =
    if (returnType == List::class.java) genericReturnType.parameterClass else null

val Method.returnTypeOrListType get() =
    if (returnType == List::class.java) returnListGenericClass else returnType

fun Method.declaredIn(clazz: Class<*>) =
    declaringClass == clazz

val Method.objectReferenceAttributeClass: Class<*>? get() =
    genericReturnType?.parameterType?.parameterClass

val <T> Class<T>.isAbstract: Boolean get() =
    Modifier.isAbstract(modifiers)

val <T> Class<T>.isNotAbstract: Boolean get() =
    !isAbstract

fun Class<*>.isSubclassOf(other: Class<*>) =
    other.isAssignableFrom(this)

private val loader get(): ClassLoader =
    Thread.currentThread().contextClassLoader

fun <T> Class<T>.nonAbstractSubclassesIn(packageName: String): List<Class<out T>> {
    val classes = subclassesIn(packageName)
    @Suppress("UNCHECKED_CAST")
    return classes.asSequence()
        .map { it as Class<out T> }
        .filter { it.isNotAbstract }
        .toList()
}

fun <T> Class<T>.nonAbstractSubclasses(): List<Class<out T>> =
    nonAbstractSubclassesIn(`package`.name)

private fun <T> Class<T>.subclassesIn(packageName: String): List<Class<*>> =
    classesIn(packageName)
        .filter { it.isSubclassOf(this) }
        .toList()

private fun classesIn(packageName: String): Sequence<Class<*>> =
    ClassPath.from(loader).getTopLevelClassesRecursive(packageName).asSequence()
        .map { classForName(it.name) }
        .filterNotNull()

fun classForName(name: String): Class<*>? =
    try { Class.forName(name) } catch (e: ClassNotFoundException) { null }