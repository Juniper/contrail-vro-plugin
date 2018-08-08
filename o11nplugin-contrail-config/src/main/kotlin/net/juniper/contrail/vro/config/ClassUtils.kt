/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import com.google.common.reflect.ClassPath
import net.juniper.contrail.api.ObjectReference
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

val <T> Class<T>.nestedName get() =
    canonicalName.replace("${`package`?.name}.", "")

val <T> Class<T>.underscoredNestedName get() =
    nestedName.replace(".", "_")

fun <T> Class<T>.wrapperName(property: String) =
    "${underscoredNestedName}_${property.capitalize()}"

val <T> Class<T>.kotlinClassName: String get() = when (this) {
    java.lang.Integer::class.java, java.lang.Integer.TYPE -> "Int"
    java.lang.Boolean.TYPE -> "Boolean"
    else -> simpleName
}

val BackRefs = "BackRefs"
val className = "[A-Za-z0-9]+"

val getPrefix = "^get".toRegex()
val backRefsPostfix = "$BackRefs$".toRegex()

val childReferencePattern = "get($className)s".toRegex()
val referencePattern = "get($className)".toRegex()
val backReferencePattern = "get($className)$BackRefs".toRegex()

val referencePatterns = sequenceOf(
    backReferencePattern,
    childReferencePattern,
    referencePattern
)

val Class<*>.refPropertyName get() =
    pluginName.decapitalize()

val Class<*>.backRefPropertyName get() =
    refPropertyName + BackRefs

val Class<*>.childRefPropertyName get() =
    refPropertyName + "s"

inline fun <reified T> asForwardRef() =
    T::class.java.refPropertyName

inline fun <reified T> asBackRef() =
    T::class.java.backRefPropertyName

inline fun <reified T> asChildRef() =
    T::class.java.childRefPropertyName

val Method.childReferenceClass get() =
    childClassName?.asObjectClass

val Method.referenceClass get() =
    referenceClassName?.asObjectClass

val Method.backReferenceClass get() =
    backReferenceClassName?.asObjectClass

val Method.isChildReferenceGetter get() =
    isReference { childReferenceClass }

val Method.isReferenceGetter get() =
    isReference { referenceClass }

val Method.isBackReferenceGetter get() =
    isReference { backReferenceClass }

private inline fun Method.isReference(referenceTypeExtractor: Method.() -> ObjectClass?) =
    referenceTypeExtractor() != null && returnListGenericClass == ObjectReference::class.java

val Method.childClassName get() =
    referredClassName(childReferencePattern)

val Method.referenceClassName get() =
    referredClassName(referencePattern)

val Method.backReferenceClassName get() =
    referredClassName(backReferencePattern)

private fun Method.referredClassName(pattern: Regex) =
    pattern.matchEntire(name)?.groupValues?.get(1)

val Method.nameWithoutGet get() =
    name.replace(getPrefix, "")

val Method.nameWithoutGetAndBackRefs get() =
    nameWithoutGet.replace(backRefsPostfix, "")

val Method.propertyName get() =
    nameWithoutGet.decapitalize()

val Method.isGetter get() =
    name.startsWith("get")

val Method.isBackRef get() =
    isGetter and name.endsWith(BackRefs)

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

val Method.isPublic get() =
    Modifier.isPublic(modifiers)

val Method.isStatic get() =
    Modifier.isStatic(modifiers)

val <T> Class<T>.isAbstract: Boolean get() =
    Modifier.isAbstract(modifiers)

val <T> Class<T>.isNotAbstract: Boolean get() =
    !isAbstract

inline fun <reified T> Class<*>?.isA() =
    this == T::class.java

fun Class<*>.isSubclassOf(other: Class<*>) =
    other.isAssignableFrom(this)

inline fun <reified T> Collection<Class<*>>.contains() =
    contains(T::class.java)

inline fun <reified T> Sequence<Class<*>>.contains() =
    contains(T::class.java)

inline fun <reified T> Class<*>?.isSubclassOf() =
    this?.isSubclassOf(T::class.java) ?: false

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

inline fun <reified T> nonAbstractSubclassesOf(): List<Class<out T>> =
    T::class.java.nonAbstractSubclasses()

private fun <T> Class<T>.subclassesIn(packageName: String): List<Class<*>> =
    classesIn(packageName)
        .filter { it.isSubclassOf(this) }
        .toList()

fun classesIn(packageName: String): Sequence<Class<*>> =
    ClassPath.from(loader).getTopLevelClassesRecursive(packageName).asSequence()
        .map { classForName(it.name) }
        .filterNotNull()

fun classForName(name: String): Class<*>? =
    try { Class.forName(name) } catch (e: ClassNotFoundException) { null }