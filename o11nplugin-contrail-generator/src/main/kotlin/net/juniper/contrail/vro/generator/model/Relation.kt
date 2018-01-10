/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.generator.util.asApiClass
import net.juniper.contrail.vro.generator.util.collapsedNestedName
import net.juniper.contrail.vro.generator.util.defaultParentType
import net.juniper.contrail.vro.generator.util.folderName
import net.juniper.contrail.vro.generator.util.isApiTypeClass
import net.juniper.contrail.vro.generator.util.isBackRef
import net.juniper.contrail.vro.generator.util.isGetter
import net.juniper.contrail.vro.generator.util.isListWrapper
import net.juniper.contrail.vro.generator.util.listWrapperGetter
import net.juniper.contrail.vro.generator.util.listWrapperGetterType
import net.juniper.contrail.vro.generator.util.nameWithoutGet
import net.juniper.contrail.vro.generator.util.nameWithoutGetAndBackRefs
import net.juniper.contrail.vro.generator.util.objectReferenceAttributeClass
import net.juniper.contrail.vro.generator.util.objectType
import net.juniper.contrail.vro.generator.util.pluralize
import net.juniper.contrail.vro.generator.util.propertyName
import net.juniper.contrail.vro.generator.util.referenceName
import net.juniper.contrail.vro.generator.util.returnListGenericClass
import net.juniper.contrail.vro.generator.util.returnsObjectReferences
import net.juniper.contrail.vro.generator.util.typeToClassName
import java.lang.reflect.Method

open class Relation (
    val parentName: String,
    val childName: String
) {
    val name: String = relationName(parentName, childName)
    val folderName = childName.folderName()
}

abstract class RefRelation (
    parentClass: ObjectClass,
    method: Method
) {
    val parentName: String = parentClass.simpleName
    val childName: String = method.nameWithoutGetAndBackRefs
    val childClass: Class<*> = childName.asApiClass!!
    val childNamePluralized = childName.pluralize()
    val getter: String = method.propertyName
    val folderName = method.nameWithoutGetAndBackRefs.folderName()
}

class ForwardRelation (
    parentClass: ObjectClass,
    method: Method
) : RefRelation(parentClass, method) {
    val attribute = method.objectReferenceAttributeClassOrDefault
    val attributeSimpleName = attribute.simpleName
    val simpleReference = attribute.isSimpleReference
}

class BackwardRelation (
    parentClass: ObjectClass,
    method: Method
) : RefRelation(parentClass, method) {
    val wrapperName: String = method.referenceName
}

fun refRelation(parentClass: ObjectClass, method: Method) =
    if (method.isBackRef) BackwardRelation(parentClass, method) else ForwardRelation(parentClass, method)

class NestedRelation(
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
    val childWrapperName = rootClass.simpleName + getterChain.joinToString("") { "_" + it.name }
    val parentWrapperName = rootClass.simpleName + parentGetterChain.joinToString("") { "_" + it.name }
    val folderName = folderNameBase.folderName()
    val toMany: Boolean = getterChain.last().toMany
}

class Getter(val name: String, val toMany: Boolean)

typealias RelationGraphNode = Pair<String, List<String>>

fun List<ObjectClass>.generateRelations(): List<Relation> {
    val parentToChildren = groupBy { it.defaultParentType }
    return asSequence()
        .map { createRelationGraphNode(it, parentToChildren) }
        .map { it.toRelationSequence() }
        .flatten().toList()
}

private fun createRelationGraphNode(
    parentClass: ObjectClass,
    parentToChildren: Map<String?, List<ObjectClass>>
): RelationGraphNode {
    val parentType = parentClass.objectType
    val children = parentToChildren.getOrElse(parentType) { listOf() }
    val childrenTypes = children.map { it.objectType.typeToClassName }
    return RelationGraphNode(parentType.typeToClassName, childrenTypes)
}

private fun relationName(parentType: String, childType: String) =
    "${parentType}To$childType"

private fun RelationGraphNode.toRelationSequence(): Sequence<Relation> =
    second.asSequence().map { Relation(first, it) }

fun List<ObjectClass>.generateReferenceRelations(): List<RefRelation> =
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
            it.isListWrapper -> {
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

private val ObjectClass.refRelations: Sequence<RefRelation> get() =
    referenceMethods.distinctBy { it.referenceName }.map { refRelation(this, it) }

private val ObjectClass.referenceMethods: Sequence<Method> get() =
    declaredMethods.asSequence()
        .filter { it.isRefMethod and it.returnsObjectReferences }

private val Method.objectReferenceAttributeClassOrDefault: Class<*> get() =
    objectReferenceAttributeClass ?: ApiPropertyBase::class.java

val Method.isRefMethod get() =
    isGetter and nameWithoutGetAndBackRefs.isApiTypeClass

private val <T> Class<T>.isSimpleReference: Boolean get() =
    this == ApiPropertyBase::class.java

private val Method.returnsListWrapperOrList: Boolean get() =
    if (returnType.isListWrapper) true
    else returnListGenericClass?.isApiTypeClass ?: false

private val Method.returnsApiPropertyOrList: Boolean get() =
    if (returnType.isApiTypeClass) true
    else returnListGenericClass?.isApiTypeClass ?: false
