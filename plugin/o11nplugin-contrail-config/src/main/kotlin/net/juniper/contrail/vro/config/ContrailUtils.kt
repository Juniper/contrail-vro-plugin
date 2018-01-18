/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
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

val String.asObjectClass get() =
    asApiClass as? ObjectClass?

val String.typeToObjectClass get() =
    typeToClassName.asObjectClass

val String.isApiTypeClass get() =
    this.asApiClass != null

val ObjectClass.defaultParentType: String? get() =
    if (isRootClass) null else newInstance().defaultParentType

val ObjectClass.objectType: String get() =
    newInstance().objectType

val Method.returnsObjectReferences: Boolean get() =
    returnListGenericClass == ObjectReference::class.java

val Class<*>.isPropertyListWrapper get() =
    superclass == ApiPropertyBase::class.java && hasOnlyListOfProperties

val Class<*>.isPropertyOrStringListWrapper get() =
    superclass == ApiPropertyBase::class.java && hasOnlyListOfPropertiesOrStrings

val Class<*>.listWrapperGetter get() =
    declaredGetters.firstOrNull { it.returnsListOfProperties }

val Class<*>.listWrapperGetterType get() =
    listWrapperGetter?.returnListGenericClass

val Class<*>.declaredGetters get() = methods.asSequence()
    .filter { it.isGetter }
    .filter { it.declaredIn(this) }
    .toList()

val Class<*>.hasOnlyListOfProperties: Boolean get() =
    declaredGetters.run {
        count() == 1 && this[0].returnsListOfProperties
    }

val Class<*>.hasOnlyListOfPropertiesOrStrings: Boolean get() =
    declaredGetters.run {
        count() == 1 && this[0].returnsListOpPropertiesOrStrings
    }

val Method.returnsListOfProperties: Boolean get() =
    returnListGenericClass?.superclass == ApiPropertyBase::class.java

val Method.returnsListOpPropertiesOrStrings: Boolean get() =
    returnListGenericClass?.run {
        this == String::class.java || superclass == ApiPropertyBase::class.java
    } ?: false

val Method.returnsApiPropertyOrList: Boolean get() =
    returnTypeOrListType?.isApiPropertyClass ?: false

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