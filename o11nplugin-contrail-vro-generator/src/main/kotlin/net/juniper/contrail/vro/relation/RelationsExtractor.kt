/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.relation

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.defaultParentType
import net.juniper.contrail.vro.generator.isRelateable
import net.juniper.contrail.vro.generator.objectClasses
import net.juniper.contrail.vro.generator.objectType

typealias RelationGraphVertex = Pair<String, List<ClassRelation>>
typealias RelationGraph = List<RelationGraphVertex>

fun extractRelations(): RelationGraph {
    val classes = objectClasses()
    return buildRelationGraph(classes)
}

private fun buildRelationGraph(classes: List<Class<out ApiObjectBase>>): RelationGraph {
    val parentToChildren = classes.groupBy { it.defaultParentType }
    return classes.asSequence()
        .filter { it.isRelateable }
        .map { createRelationGraphVertex(it, parentToChildren) }
        .toList()
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
