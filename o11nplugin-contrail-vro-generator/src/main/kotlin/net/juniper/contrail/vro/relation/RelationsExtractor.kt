/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.relation

import net.juniper.contrail.api.ApiObjectBase

typealias RelationGraphVertex = Pair<String, List<Relation>>
typealias RelationGraph = List<RelationGraphVertex>

fun extractRelations(): RelationGraph {
    val classes =
        ApiObjectBase::class.java.nonAbstractSubclassesIn(ApiObjectBase::class.java.`package`.name)
    return buildRelationGraph(classes)
}

private fun buildRelationGraph(classes: List<Class<out ApiObjectBase>>): RelationGraph {
    val parentToChildren = classes.groupBy { it.defaultParentType }
    return classes.map { createRelationGraphVertex(it, parentToChildren) }
}

private fun createRelationGraphVertex(
    parentClass: Class<out ApiObjectBase>,
    parentToChildren: Map<String?, List<Class<out ApiObjectBase>>>
): RelationGraphVertex {
    val parentType = parentClass.objectType
    val children = parentToChildren.getOrElse(parentType) { listOf() }
    val childrenTypes = children.map { it.objectType }
    val childrenRelations = childrenTypes.map { buildRelation(parentType, it) }
    return Pair(parentType, childrenRelations)
}
