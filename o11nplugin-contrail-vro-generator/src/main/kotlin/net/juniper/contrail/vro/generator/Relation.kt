/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ObjectReference
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class Relation (
    val parentName: String,
    val childName: String
) {
    val name: String = relationName(parentName, childName)
    val childNameDecapitalized = childName.decapitalize()
    val folderName = childName.folderName()
}

class RefRelation (
    parentClass: Class<out ApiObjectBase>,
    method: Method
) {
    val parentName: String = parentClass.simpleName
    val childName: String = method.referenceName
    val getter: String = method.propertyName
}

class NestedRelation(
    val parent: Class<*>,
    val child: Class<*>,
    val getter: String,
    val simpleProperties: List<Property>,
    val listProperties: List<Property>,
    val getterChain: List<String>,
    val listStatusChain: List<Boolean>,
    val rootClass: Class<*>,
    val toMany: Boolean = false
) {
    val parentName: String = parent.nestedName
    val childName: String = child.nestedName
    val parentCollapsedName = parent.collapsedNestedName
    val childCollapsedName = child.collapsedNestedName
    val name: String = relationName(parentCollapsedName, getter)
    val getterSplitCamel = getter.splitCamel()
    val getterDecapitalized = getter.decapitalize()
    val childWrapperName = rootClass.simpleName + "_" + getterChain.joinToString("_")
    val parentWrapperName = rootClass.simpleName + getterChain.dropLast(1).joinToString("") { "_" + it }
    val getterChainWithStatus = getterChain.zip(listStatusChain).map { GetterWithMultiplinessStatus(it.first, it.first.decapitalize(), it.second) }
}

class GetterWithMultiplinessStatus(val getterName: String, val getterDecap: String, val getterStatus: Boolean)

private fun RelationGraphVertex.asRelationList(): List<Relation> {
    return second.map {
        Relation(
            it.name.typeToClassName(),
            first.typeToClassName(),
            it.childTypeName.typeToClassName()
        )
    }
}

typealias RelationGraphNode = Pair<String, List<String>>

fun generateRelations(classes: List<Class<out ApiObjectBase>>): List<Relation> {
    val parentToChildren = classes.groupBy { it.defaultParentType }
    return classes.asSequence()
        .filter { it.isRelatable }
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
    val childrenTypes = children.map { it.objectType.dashedToCamelCase() }
    return RelationGraphNode(parentType.dashedToCamelCase(), childrenTypes)
}

private fun relationName(parentType: String, childType: String) =
    "${parentType}To$childType"

private fun RelationGraphNode.toRelationSequence(): Sequence<Relation> =
    second.asSequence().map { Relation( first, it) }

fun generateReferenceRelations(classes: List<Class<out ApiObjectBase>>): List<RefRelation> =
    classes.asSequence()
        .map { it.refRelations }
        .flatten()
        .toList()

fun generateNestedRelations(classes: List<Class<*>>): List<NestedRelation> =
    classes.asSequence()
        .map { it.nestedRelations(classes, listOf(), listOf(), it) }
        .flatten()
        .toList()


private fun Class<*>.nestedRelations(baseClasses: List<Class<*>>, chainSoFar: List<String>, listChainSoFar: List<Boolean>, rootClass: Class<*>): Sequence<NestedRelation> =
    methods.asSequence()
        .filter { it.isGetter && it.returnsApiPropertyOrList }
        .map { it.recursiveRelations(baseClasses, chainSoFar, listChainSoFar, rootClass) }
        .flatten()

private fun Method.recursiveRelations(baseClasses: List<Class<*>>, chainSoFar: List<String>, listChainSoFar: List<Boolean>, rootClass: Class<*>): Sequence<NestedRelation> {
    val wrapperChildName = nameWithoutGet

    val (childType, toMany) = when (returnType) {
        List::class.java -> Pair(returnListGenericType!!, true)
        else -> Pair(returnType, false)
    }

    val newChain = chainSoFar + wrapperChildName
    val newListChain = listChainSoFar + toMany

    val relation = NestedRelation(
        declaringClass,
        childType,
        wrapperChildName,
        childType.properties.simpleProperties,
        childType.properties.listProperties,
        newChain,
        newListChain,
        rootClass,
        toMany)

    return if (baseClasses.contains(childType))
        sequenceOf(relation)
    else
        childType.nestedRelations(baseClasses, newChain, newListChain, rootClass) + relation
}

private val <T: ApiObjectBase> Class<T>.refRelations: Sequence<RefRelation> get() =
    referenceMethods.distinctBy { it.referenceName }.map { RefRelation(this, it) }

private val <T: ApiObjectBase> Class<T>.referenceMethods: Sequence<Method> get() =
    declaredMethods.asSequence()
        .filter { it.isRefMethod && it.returnsObjectReferences }

private val Type.parameterType: Class<*>? get() =
    if (this is ParameterizedType) actualTypeArguments[0] as? Class<*> else null

private val Method.isRefMethod get() =
    isGetter && nameWithoutGetAndBackRefs.isApiTypeClass

private val Method.returnListGenericType: Class<*>? get() =
    if (returnType == List::class.java) genericReturnType.parameterType else null

private val Method.returnsObjectReferences: Boolean get() =
    returnListGenericType == ObjectReference::class.java

private val Method.returnsApiPropertyOrList: Boolean get() =
    if (returnType.isApiTypeClass) true
    else returnListGenericType?.isApiTypeClass ?: false
