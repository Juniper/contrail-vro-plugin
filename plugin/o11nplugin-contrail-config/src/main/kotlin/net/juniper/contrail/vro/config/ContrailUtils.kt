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

val String.typeToObjectClass get() =
    typeToClassName.asObjectClass

val String.isApiTypeClass get() =
    this.asApiClass != null

val ObjectClass.defaultParentType: String? get() =
    newInstance().defaultParentType

val ObjectClass.parentType: String? get() = when {
    isRootClass -> null
    // Default parent of Virtual Machine Interface is deprecated in the schema
    isA<VirtualMachineInterface>() -> "project"
    else -> defaultParentType
}

val ObjectClass.objectType: String get() =
    newInstance().objectType

val Method.returnsObjectReferences: Boolean get() =
    returnListGenericClass.isA<ObjectReference<*>>()

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

val Class<*>.hasOnlyListOfPropertiesOrStrings: Boolean get() =
    declaredGetters.run {
        count() == 1 && this[0].returnsListOpPropertiesOrStrings
    }

val Method.returnsListOfProperties: Boolean get() =
    returnListGenericClass?.isSubclassOf<ApiPropertyBase>() ?: false

val Method.returnsListOpPropertiesOrStrings: Boolean get() =
    returnListGenericClass?.run {
        this.isA<String>() || this.isSubclassOf<ApiPropertyBase>()
    } ?: false

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