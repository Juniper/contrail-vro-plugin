/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.relation.RelationGraphVertex
import net.juniper.contrail.vro.relation.buildRelationGraph

class Relation (
    val name: String,
    val parentClassName: String,
    val childClassName: String
) {
    val childClassNameDecapitalized = childClassName.decapitalize()
    val childClassNameSplitCamel = childClassName.splitCamel()
    val folderName = childClassName.folderName()
}

fun generateRelationStatements(classes: List<Class<out ApiObjectBase>>): List<Relation> {
    val relationsGraph = buildRelationGraph(classes)
    return relationsGraph.map { it.asRelationList() }.flatten()
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
