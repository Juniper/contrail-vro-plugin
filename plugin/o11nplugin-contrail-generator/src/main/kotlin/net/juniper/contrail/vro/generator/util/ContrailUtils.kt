/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.util

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.vro.generator.apiPackageName
import net.juniper.contrail.vro.generator.apiTypesPackageName
import net.juniper.contrail.vro.generator.isRootClass
import net.juniper.contrail.vro.generator.model.ObjectClass
import net.juniper.contrail.vro.generator.model.PropertyClass
import java.lang.reflect.Method

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclasses()

val Class<*>.isApiObjectClass get() =
    ApiObjectBase::class.java.isAssignableFrom(this)

val Class<*>.isApiPropertyClass get() =
    ApiPropertyBase::class.java.isAssignableFrom(this)

val Class<*>.isApiTypeClass get() =
    isApiObjectClass || isApiPropertyClass

val String.asApiClass get() =
    classForName("$apiTypesPackageName.$this") ?: classForName("$apiPackageName.$this")

val String.isApiTypeClass get() =
    this.asApiClass != null

val ObjectClass.defaultParentType: String? get() =
    if (isRootClass) null else newInstance().defaultParentType

val ObjectClass.objectType: String get() =
    newInstance().objectType

val ObjectClass.parentClassName: String? get() =
    defaultParentType?.typeToClassName

val Method.returnsObjectReferences: Boolean get() =
    returnListGenericClass == ObjectReference::class.java

val Class<*>.isListWrapper get() =
    superclass == ApiPropertyBase::class.java && hasOnlyListOfProperties

val Class<*>.listWrapperGetter get() =
    listOfPropertiesGetters.firstOrNull()

val Class<*>.listWrapperGetterType get() =
    listWrapperGetter?.returnListGenericClass

val Class<*>.declaredGetters get() = methods.asSequence()
    .filter { it.isGetter }
    .filter { it.declaredIn(this) }

val Class<*>.listOfPropertiesGetters get() =
    declaredGetters
    .filter { it.returnsListOfProperties }

val Class<*>.hasOnlyListOfProperties: Boolean get() =
    declaredGetters.count() == 1 &&
    listOfPropertiesGetters.count() == 1

val Method.returnsListOfProperties: Boolean get() =
    returnListGenericClass?.superclass == ApiPropertyBase::class.java

private val Method.deepestReturnType: Class<*>? get() =
    if (returnType == List::class.java)
        objectReferenceAttributeClass ?: returnListGenericClass
    else
        returnType

private val Method.apiPropertyReturn get() =
    deepestReturnType?.let {
        if (it.superclass == ApiPropertyBase::class.java) it as PropertyClass else null
    }

private val Array<Method>.asProperties get() =
    asSequence().filter { it.isGetter }.map { it.apiPropertyReturn }.filterNotNull()

private val Sequence<PropertyClass>.recursed get() =
    map { it.apiProperties }.flatten()

private val PropertyClass.apiProperties: Sequence<PropertyClass> get() =
    sequenceOf(this) + methods.asProperties.recursed

val ObjectClass.apiPropertyClasses get() =
    methods.asProperties.recursed

fun List<ObjectClass>.propertyClasses() = asSequence()
    .map { it.apiPropertyClasses }
    .flatten()
    .distinct()
    .toList()