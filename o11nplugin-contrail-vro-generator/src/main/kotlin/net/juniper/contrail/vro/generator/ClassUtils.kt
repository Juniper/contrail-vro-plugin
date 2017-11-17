/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.google.common.reflect.ClassPath
import java.lang.reflect.Modifier

fun <T> Class<T>.nonAbstractSubclassesIn(packageName: String): List<Class<out T>> {
    val classes = subclassesIn(packageName)
    @Suppress("UNCHECKED_CAST")
    return classes.map { it as Class<out T> }
        .filter { it.isNotAbstract }
}

fun <T> Class<T>.nonAbstractSubclasses(): List<Class<out T>> =
    nonAbstractSubclassesIn(`package`.name)

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
