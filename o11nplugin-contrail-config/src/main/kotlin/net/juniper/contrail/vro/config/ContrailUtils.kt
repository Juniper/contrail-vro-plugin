/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.constants.apiPackageName
import net.juniper.contrail.vro.config.constants.apiTypesPackageName
import java.lang.reflect.Method

fun objectClasses() =
    nonAbstractSubclassesOf<ApiObjectBase>()

val Class<*>.isApiObjectClass get() =
    isSubclassOf<ApiObjectBase>()

val Class<*>.isApiPropertyClass get() =
    isSubclassOf<ApiPropertyBase>()

val Class<*>.isApiTypeClass get() =
    isApiObjectClass || isApiPropertyClass

val String.asApiClass get() =
    classForName("$apiTypesPackageName.$this") ?: classForName("$apiPackageName.$this")

val String.asObjectClass: ObjectClass? get() =
    asApiClass?.let {
        @Suppress("UNCHECKED_CAST")
        if (it.isSubclassOf<ApiObjectBase>()) it as ObjectClass else null
    }

val String.asPropertyClass: PropertyClass? get() =
    asApiClass?.let {
        @Suppress("UNCHECKED_CAST")
        if (it.isSubclassOf<ApiPropertyBase>()) it as PropertyClass else null
    }

val String.isApiTypeClass get() =
    asApiClass != null

val String.isApiObjectClass get() =
    asObjectClass != null

val String.isApiPropertyClass get() =
    asPropertyClass != null

val ObjectClass.objectType: String get() =
    newInstance().objectType

val ObjectClass.defaultParentType: String? get() =
    newInstance().defaultParentType

fun ObjectClass.parentType( config: Config) : String? = when {
    config.isRootClass(this) -> null
    // Default parent of Virtual Machine Interface is deprecated in the schema
    isA<VirtualMachineInterface>() -> "project"
    else -> defaultParentType
}

val Method.returnsObjectReferences: Boolean get() =
    returnListGenericClass.isA<ObjectReference<*>>()

val Class<*>.isStringListWrapper get() =
    isSubclassOf<ApiPropertyBase>() && hasOnlyListOfStrings

val Class<*>.isPropertyListWrapper get() =
    isSubclassOf<ApiPropertyBase>() && hasOnlyListOfProperties

val Class<*>.isPropertyOrStringListWrapper get() =
    isSubclassOf<ApiPropertyBase>() && hasOnlyListOfPropertiesOrStrings

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

val Class<*>.hasOnlyListOfStrings: Boolean get() =
    declaredGetters.run {
        count() == 1 && this[0].returnsListOfStrings
    }

val Class<*>.hasOnlyListOfPropertiesOrStrings: Boolean get() =
    hasOnlyListOfProperties || hasOnlyListOfStrings

val Method.returnsListOfProperties: Boolean get() =
    returnListGenericClass?.isSubclassOf<ApiPropertyBase>() ?: false

val Method.returnsListOfStrings: Boolean get() =
    returnListGenericClass == String::class.java

val Method.returnsListOfPropertiesOrStrings: Boolean get() =
    returnsListOfProperties || returnsListOfStrings

val Method.returnsApiPropertyOrList: Boolean get() =
    returnTypeOrListType?.isApiPropertyClass ?: false

private val Method.deepestReturnType: Class<*>? get() =
    if (returnType.isA<List<*>>())
        objectReferenceAttributeClass ?: returnListGenericClass
    else
        returnType

private val Method.apiPropertyReturn get() =
    deepestReturnType?.let {
        @Suppress("UNCHECKED_CAST")
        if (it.isSubclassOf<ApiPropertyBase>()) it as PropertyClass else null
    }

private val Array<Method>.asProperties get() =
    asSequence().filter { it.isGetter }.map { it.apiPropertyReturn }.filterNotNull().filter { it.isNotAbstract }

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