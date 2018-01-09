/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.generator.util.asApiClass
import net.juniper.contrail.vro.generator.util.collapsedNestedName
import net.juniper.contrail.vro.generator.util.defaultParentType
import net.juniper.contrail.vro.generator.util.folderName
import net.juniper.contrail.vro.generator.util.isApiTypeClass
import net.juniper.contrail.vro.generator.util.isBackRef
import net.juniper.contrail.vro.generator.util.isGetter
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
    parentClass: Class<out ApiObjectBase>,
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
    parentClass: Class<out ApiObjectBase>,
    method: Method
) : RefRelation(parentClass, method) {
    val referenceAttribute = method.objectReferenceAttributeClassOrDefault
    val referenceAttributeSimpleName = referenceAttribute.simpleName
    val simpleReference = referenceAttribute.isSimpleReference
}

class BackwardRelation (
    parentClass: Class<out ApiObjectBase>,
    method: Method
) : RefRelation(parentClass, method) {
    val wrapperName: String = method.referenceName
}

fun refRelation(parentClass: Class<out ApiObjectBase>, method: Method) =
    if (method.isBackRef) BackwardRelation(parentClass, method) else ForwardRelation(parentClass, method)

class NestedRelation(
    val parent: Class<*>,
    val child: Class<*>,
    val simpleProperties: List<Property>,
    val listProperties: List<Property>,
    val getterChain: List<Getter>,
    val rootClass: Class<*>
) {
    val parentCollapsedName = parent.collapsedNestedName
    val getter: String = getterChain.last().name
    val getterDecapitalized = getter.decapitalize()
    val name: String = relationName(parentCollapsedName, getter)
    val childWrapperName = rootClass.simpleName + getterChain.joinToString("") { "_" + it.name }
    val parentWrapperName = rootClass.simpleName + getterChain.dropLast(1).joinToString("") { "_" + it.name }
    val folderName = child.simpleName.folderName()
    val toMany: Boolean = getterChain.last().toMany
}

class Getter(val name: String, val toMany: Boolean)

typealias RelationGraphNode = Pair<String, List<String>>

fun generateRelations(classes: List<Class<out ApiObjectBase>>): List<Relation> {
    val parentToChildren = classes.groupBy { it.defaultParentType }
    return classes.asSequence()
        .map { createRelationGraphNode(it, parentToChildren) }
        .map { it.toRelationSequence() }
        .flatten().toList()
}

private fun createRelationGraphNode(
    parentClass: Class<out ApiObjectBase>,
    parentToChildren: Map<String?, List<Class<out ApiObjectBase>>>
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

fun generateReferenceRelations(classes: List<Class<out ApiObjectBase>>): List<RefRelation> =
    classes.asSequence()
        .map { it.refRelations }
        .flatten()
        .filter { classes.contains(it.childClass) }
        .toList()

fun generateNestedRelations(classes: List<Class<*>>): List<NestedRelation> =
    classes.asSequence()
        .map { it.nestedRelations(classes, listOf(), it) }
        .flatten()
        .toList()

private fun Class<*>.nestedRelations(
    baseClasses: List<Class<*>>,
    chainSoFar: List<Getter>,
    rootClass: Class<*>
): Sequence<NestedRelation> =
    methods.asSequence()
        .filter { it.isGetter and it.returnsApiPropertyOrList }
        .map { it.recursiveRelations(baseClasses, chainSoFar, rootClass) }
        .flatten()

private fun Method.recursiveRelations(
    baseClasses: List<Class<*>>,
    chainSoFar: List<Getter>,
    rootClass: Class<*>
): Sequence<NestedRelation> {
    val wrapperChildName = nameWithoutGet

    val (childType, toMany) = when (returnType) {
        List::class.java -> Pair(returnListGenericClass!!, true)
        else -> Pair(returnType, false)
    }

    val newChain = chainSoFar + Getter(wrapperChildName, toMany)

    val relation = NestedRelation(
        declaringClass,
        childType,
        childType.properties.simpleProperties,
        childType.properties.listProperties,
        newChain,
        rootClass
    )

    return if (baseClasses.contains(childType))
        sequenceOf(relation)
    else
        childType.nestedRelations(baseClasses, newChain, rootClass) + relation
}

private val <T: ApiObjectBase> Class<T>.refRelations: Sequence<RefRelation> get() =
    referenceMethods.distinctBy { it.referenceName }.map { refRelation(this, it) }

private val <T: ApiObjectBase> Class<T>.referenceMethods: Sequence<Method> get() =
    declaredMethods.asSequence()
        .filter { it.isRefMethod and it.returnsObjectReferences }

private val Method.objectReferenceAttributeClassOrDefault: Class<*> get() =
    objectReferenceAttributeClass ?: ApiPropertyBase::class.java

val Method.isRefMethod get() =
    isGetter and nameWithoutGetAndBackRefs.isApiTypeClass

private val <T> Class<T>.isSimpleReference: Boolean get() =
    this == ApiPropertyBase::class.java

private val Method.returnsApiPropertyOrList: Boolean get() =
    if (returnType.isApiTypeClass) true
    else returnListGenericClass?.isApiTypeClass ?: false
