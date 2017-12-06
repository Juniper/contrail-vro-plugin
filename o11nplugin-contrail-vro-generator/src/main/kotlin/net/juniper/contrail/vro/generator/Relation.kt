/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import java.lang.reflect.Method

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
    val getter: String
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

fun generateNestedRelations(classes: List<Class<out ApiObjectBase>>): List<NestedRelation> {
    return classes.asSequence()
        .map { it.nestedRelations() }
        .flatten()
        .toList()
}


private typealias NestedClassRelationInfo = Pair<Class<*>, String>

private fun Method.toRelationInfo(): NestedClassRelationInfo =
    NestedClassRelationInfo(returnType, name.replaceFirst("get", ""))

private fun Class<*>.nestedRelations(): Sequence<NestedRelation> =
    nestedRelationInfos()
        .map { NestedRelation(this, it.first, it.second) }

private fun Class<*>.nestedRelationInfos(): Sequence<NestedClassRelationInfo> =
    methods.asSequence()
        .filter { it.name.startsWith("get") }
        .filter { it.returnType.isApiClass }
        .map { it.toRelationInfo() }
