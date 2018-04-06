/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.config.BackRefs
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClass
import net.juniper.contrail.vro.config.PropertyClassFilter
import net.juniper.contrail.vro.config.asObjectClass
import net.juniper.contrail.vro.config.childClassName
import net.juniper.contrail.vro.config.collapsedNestedName
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isBackRef
import net.juniper.contrail.vro.config.isChildReferenceGetter
import net.juniper.contrail.vro.config.isDisplayableChildOf
import net.juniper.contrail.vro.config.isGetter
import net.juniper.contrail.vro.config.isInReversedRelationTo
import net.juniper.contrail.vro.config.isModelClassName
import net.juniper.contrail.vro.config.isPropertyListWrapper
import net.juniper.contrail.vro.config.listWrapperGetter
import net.juniper.contrail.vro.config.listWrapperGetterType
import net.juniper.contrail.vro.config.nameWithoutGet
import net.juniper.contrail.vro.config.nameWithoutGetAndBackRefs
import net.juniper.contrail.vro.config.objectReferenceAttributeClass
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.pluralize
import net.juniper.contrail.vro.config.refPropertyName
import net.juniper.contrail.vro.config.returnListGenericClass
import net.juniper.contrail.vro.config.returnsApiPropertyOrList
import net.juniper.contrail.vro.config.returnsObjectReferences
import net.juniper.contrail.vro.config.toPluginName
import java.lang.reflect.Method

open class Relation (
    val parentClass: ObjectClass,
    val childClass: ObjectClass
) {
    val parentName: String = parentClass.simpleName
    val childName: String = childClass.simpleName
    val name: String = relationName(parentName, childName)
    val folderName = childClass.pluginName.folderName()
}

class ForwardRelation (
    val declaredParentClass: ObjectClass,
    method: Method
) {
    val declaredChildClass = method.nameWithoutGetAndBackRefs.asObjectClass!!
    val isReversed: Boolean = declaredParentClass.isInReversedRelationTo(declaredChildClass)
    val parentClass: ObjectClass = if (isReversed) declaredChildClass else declaredParentClass
    val childClass: ObjectClass = if (isReversed) declaredParentClass else declaredChildClass
    val parentName: String = parentClass.simpleName
    val childName: String = childClass.simpleName
    val parentPluginName: String = parentName.toPluginName
    val childPluginName: String = childName.toPluginName
    val childNamePluralized = childPluginName.pluralize()
    val pluginGetter = declaredChildClass.refPropertyName
    val getter: String = childName.decapitalize() + if (isReversed) BackRefs else ""
    val folderName = childName.folderName()
    val attribute = method.objectReferenceAttributeClassOrDefault
    val attributeSimpleName = attribute.simpleName
    val simpleReference = attribute.isSimpleReference
}

class NestedRelation (
    val parent: Class<*>,
    val child: PropertyClass,
    val simpleProperties: List<Property>,
    val listProperties: List<Property>,
    val getterChain: List<Getter>,
    parentGetterChain: List<Getter>,
    val rootClass: Class<*>,
    folderNameBase: String
) {
    val parentCollapsedName = parent.collapsedNestedName
    val getter: String = getterChain.last().name
    val getterDecapitalized = getter.decapitalize()
    val name: String = relationName(parentCollapsedName, getter)
    val childWrapperName = wrapperName(rootClass, getterChain)
    val parentWrapperName = wrapperName(rootClass, parentGetterChain)
    val folderName = folderNameBase.folderName()
    val toMany: Boolean = getterChain.last().toMany
}

class Getter(val name: String, val toMany: Boolean)

fun List<ObjectClass>.generateRelations() = asSequence()
    .map { it.relations() }
    .flatten().toList()

private fun ObjectClass.relations() = methods.asSequence()
    .filter { it.isChildReferenceGetter }
    .map { it.childClassName }.filterNotNull()
    .filter { it.isModelClassName }
    .map { it.asObjectClass }.filterNotNull()
    .map { Relation(this, it) }

private fun relationName(parentType: String, childType: String) =
    "${parentType}To$childType"

fun List<ObjectClass>.generateReferenceRelations(): List<ForwardRelation> =
    asSequence()
        .map { it.refRelations }
        .flatten()
        .filter { contains(it.childClass) }
        .toList()

fun List<ObjectClass>.generateNestedRelations(propertyFilter: PropertyClassFilter): List<NestedRelation> =
    asSequence()
        .map { it.nestedRelations(listOf(), it, propertyFilter) }
        .flatten()
        .toList()

private fun Class<*>.nestedRelations(
    chainSoFar: List<Getter>,
    rootClass: ObjectClass,
    propertyFilter: PropertyClassFilter
): Sequence<NestedRelation> =
    methods.asSequence()
        .filter { it.isGetter and it.returnsApiPropertyOrList }
        .filter { propertyFilter(it.returnType as PropertyClass) }
        .map { it.recursiveRelations(chainSoFar, rootClass, propertyFilter) }
        .flatten()

private fun Method.recursiveRelations(
    chainSoFar: List<Getter>,
    rootClass: ObjectClass,
    propertyFilter: PropertyClassFilter
): Sequence<NestedRelation> {
    val newChain = chainSoFar.toMutableList()
    val childType = returnType.let {
        when {
            it.isPropertyListWrapper -> {
                newChain += Getter(nameWithoutGet, false)
                newChain += Getter(it.listWrapperGetter!!.nameWithoutGet, true)
                it.listWrapperGetterType!!
            }
            it == List::class.java -> {
                newChain += Getter(nameWithoutGet, true)
                returnListGenericClass!!
            }
            else -> {
                newChain += Getter(nameWithoutGet, false)
                it
            }
        }
    } as PropertyClass

    val relation = NestedRelation(
        declaringClass,
        childType,
        childType.properties.simpleProperties,
        childType.properties.listProperties,
        newChain,
        chainSoFar,
        rootClass,
        nameWithoutGet
    )

    return childType.nestedRelations(newChain, rootClass, propertyFilter) + relation
}

private val ObjectClass.refRelations: Sequence<ForwardRelation> get() =
    referenceMethods
        .filter { ! it.isBackRef }
        .map { ForwardRelation(this, it) }
        .filter { it.childName isDisplayableChildOf it.parentName }

private val ObjectClass.referenceMethods: Sequence<Method> get() =
    declaredMethods.asSequence()
        .filter { it.isRefMethod and it.returnsObjectReferences }

private val Method.objectReferenceAttributeClassOrDefault: Class<*> get() =
    objectReferenceAttributeClass ?: ApiPropertyBase::class.java

val Method.isRefMethod get() =
    isGetter and nameWithoutGetAndBackRefs.isApiTypeClass

private val <T> Class<T>.isSimpleReference: Boolean get() =
    this == ApiPropertyBase::class.java

