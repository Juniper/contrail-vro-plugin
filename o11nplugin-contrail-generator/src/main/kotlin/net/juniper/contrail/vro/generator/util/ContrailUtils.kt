/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.util

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.vro.generator.apiTypesPackageName
import java.lang.reflect.Method

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclasses()

val Class<*>.isApiTypeClass get() =
    `package` != null && `package`.name == apiTypesPackageName

val String.asApiClass get() =
    classForName("$apiTypesPackageName.$this")

val String.isApiTypeClass get() =
    this.asApiClass != null

val Class<out ApiObjectBase>.defaultParentType: String? get() =
    newInstance().defaultParentType

val Class<out ApiObjectBase>.objectType: String get() =
    newInstance().objectType

val Class<out ApiObjectBase>.parentClassName: String? get() =
    defaultParentType?.typeToClassName

val Method.returnsObjectReferences: Boolean get() =
    returnListGenericClass == ObjectReference::class.java

private val Method.deepestReturnType: Class<*>? get() =
    if (returnType == List::class)
        returnListGenericClass ?: objectReferenceAttributeClass
    else
        returnType

private val Method.apiPropertyReturn get() =
    deepestReturnType?.let {
        if (it.superclass == ApiPropertyBase::class) it as Class<out ApiPropertyBase> else null
    }

private val Array<Method>.asProperties get() =
    asSequence().filter { it.isGetter }.map { it.apiPropertyReturn }.filterNotNull()

private val Sequence<Class<out ApiPropertyBase>>.recursed get() =
    map { it.apiProperties }.flatten()

private val Class<out ApiPropertyBase>.apiProperties: Sequence<Class<out ApiPropertyBase>> get() =
    sequenceOf(this) + methods.asProperties.recursed

val Class<out ApiObjectBase>.apiPropertyClasses get() =
    methods.asProperties.recursed

fun List<Class<out ApiObjectBase>>.propertyClasses() = asSequence()
    .map { it.apiPropertyClasses }
    .flatten()
    .distinct()
    .toList()