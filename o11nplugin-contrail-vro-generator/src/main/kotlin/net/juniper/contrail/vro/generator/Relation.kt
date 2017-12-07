/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

open class Relation (
    val parentName: String,
    val childName: String
) {
    val name: String = relationName(parentName, childName)
    val childNameDecapitalized = childName.decapitalize()
    val folderName = childClassName.folderName()
}

class NestedRelation(
    parent: Class<*>,
    child: Class<*>,
    val getter: String,
    simpleProperties: List<Property>,
    listProperties: List<Property>,
    val getterChain: List<String>,
    val toMany: Boolean = false
) {
    val parentName: String = parent.nestedName
    val childName: String = child.nestedName
    val parentCollapsedName = parent.collapsedNestedName
    val childCollapsedName = child.collapsedNestedName
    val name: String = relationName(parentCollapsedName, getter)
    val getterSplitCamel = getter.splitCamel()
    val getterDecapitalized = getter.decapitalize()
}

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
        .map { it.asRelationSequence() }
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

private fun RelationGraphNode.asRelationSequence(): Sequence<Relation> =
    second.asSequence().map { Relation( first, it) }

fun generateNestedRelations(classes: List<Class<*>>): List<NestedRelation> {
    return classes.asSequence()
        .map { it.nestedRelations(classes, listOf()) }
        .flatten()
        .toList()
}


private fun Class<*>.nestedRelations(baseClasses: List<Class<*>>, chainSoFar: List<String>): Sequence<NestedRelation> =
    methods.asSequence()
        .filter { it.name.startsWith("get") }
        .filter { it.isRelevantType }
        .map { it.recursiveRelations(baseClasses, chainSoFar) }
        .flatten()

private fun Method.recursiveRelations(baseClasses: List<Class<*>>, chainSoFar: List<String>): Sequence<NestedRelation> {
    val wrapperChildName = name.replaceFirst("get", "")

    val (childType, toMany) = when (returnType) {
        List::class.java -> Pair(listGenericType, true)
        else -> Pair(returnType, false)
    }

    val newChain = chainSoFar + wrapperChildName

    val rel = NestedRelation(
        declaringClass,
        childType,
        wrapperChildName,
        listOf(),
        listOf(),
        newChain,
        toMany)
    /*
    class NestedRelation(
        parent: Class<*>,
        child: Class<*>,
        getter: String,
        simpleProperties: List<Property>,
        listProperties: List<Property>,
        getterChain: List<String>,
        val toMany: Boolean = false
    ) {
    */
    return if (baseClasses.contains(childType)) {
        sequenceOf()
    } else {
        childType.nestedRelations(baseClasses, newChain)
    } + rel
}

private val Method.isRelevantType: Boolean get() {
    if (returnType.isApiClass) return true
    return listGenericType.isApiClass
}

private val Method.listGenericType: Class<*> get() {
    if (returnType == java.util.List::class.java) {
        val genericType = genericReturnType as ParameterizedType
        genericType.actualTypeArguments[0] as? ParameterizedType ?: return genericType.actualTypeArguments[0] as Class<*>
    }
    return Object::class.java // in case of List<ObjectReference<*>>
}