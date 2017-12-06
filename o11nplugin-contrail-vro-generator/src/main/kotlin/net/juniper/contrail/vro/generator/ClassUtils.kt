/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.google.common.reflect.ClassPath
import net.juniper.contrail.api.ApiObjectBase
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

fun <T> Class<T>.nonAbstractSubclassesIn(packageName: String): List<Class<out T>> {
    val classes = subclassesIn(packageName)
    @Suppress("UNCHECKED_CAST")
    return classes.map { it as Class<out T> }
        .filter { it.isNotAbstract }
}

fun <T> Class<T>.nonAbstractSubclasses(): List<Class<out T>> =
    nonAbstractSubclassesIn(`package`.name)

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

val <T> Class<T>.isAbstract: Boolean get() =
    Modifier.isAbstract(modifiers)

val <T> Class<T>.isNotAbstract: Boolean get() =
    !isAbstract

private val loader get(): ClassLoader =
    Thread.currentThread().contextClassLoader

private fun <T> Class<T>.subclassesIn(packageName: String): List<Class<*>> =
    classesIn(packageName)
        .filter { it.superclass == this }
        .toList()

fun Class<*>.innerClassTree(includeThis: Boolean = true): Sequence<Class<*>> {
    val root = if (includeThis) sequenceOf(this) else emptySequence()
    return root + declaredClasses.asSequence().map { it.innerClassTree() }.flatten()
}

private fun List<Class<*>>.innerClasses(includeThis: Boolean): Sequence<Class<*>> =
    asSequence().map { it.innerClassTree(includeThis) }.flatten()

fun List<Class<*>>.allInnerClasses(): Sequence<Class<*>> =
    innerClasses(true)

fun List<Class<*>>.strictlyInnerClasses(): Sequence<Class<*>> =
    innerClasses(false)

private fun classesIn(packageName: String): Sequence<Class<*>> =
    ClassPath.from(loader).getTopLevelClassesRecursive(packageName).asSequence()
        .map { it.name }
        .map { classForName(it) }
        .filterNotNull()

private fun classForName(name: String): Class<*>? {
    return try {
        Class.forName(name)
    } catch (e: ClassNotFoundException) {
        null
    }
}
private fun Field.isListOfInnerClasses(innerClasses: List<Class<*>>): Boolean {
    if (this.type != List::class.java) return false
    return innerClasses.contains(listFieldParameterType())
}

private fun Field.listFieldParameterType(): Class<*> {
    val parameterizedType = genericType as ParameterizedType
    return parameterizedType.actualTypeArguments[0] as Class<*>
}

fun Class<*>.toClassInfo() =
    ClassInfo(this.simpleName)

val Class<*>.xsdName : String
    get() = simpleName.splitCamel().toLowerCase().replace(" ", "-")

val <T> Class<T>.xsdType: String get() = when (this.simpleName) {
    "String" -> "string"
    "Boolean" -> "boolean"
    "Int" -> "number"
    else -> ""
}

fun Iterable<Class<*>>.toClassInfo() =
    map { it.toClassInfo() }

fun Class<*>.toNestedClassInfo() =
    NestedClassInfo(this)

fun Iterable<Class<*>>.toNestedClassInfo() =
    map { it.toNestedClassInfo() }

fun Class<*>.toConverterInfo() =
    ConverterInfo(this)

fun Iterable<Class<*>>.toConverterInfo() =
    map { it.toConverterInfo() }

class ClassInfo(val simpleName: String) {
    val folderName get() = simpleName.folderName()
}

class NestedClassInfo(clazz: Class<*>) {
    val canonicalName = clazz.canonicalName
    val simpleName = clazz.simpleName
    val nestedName = clazz.nestedName
}

class ConverterInfo(
    targetClass: Class<*>
) {
    val proxyName = targetClass.simpleName
    val targetName = targetClass.canonicalName
    val targetCollapsedName = targetClass.collapsedNestedName
}

val Class<out ApiObjectBase>.hasParent
    get() = this.newInstance().defaultParentType != null