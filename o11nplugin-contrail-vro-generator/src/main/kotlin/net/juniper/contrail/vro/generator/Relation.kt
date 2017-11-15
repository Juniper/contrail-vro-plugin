/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.relation.extractRelations

class Relation
(
    val name: String,
    val parentClassName: String,
    val childClassName: String
) {
    val childClassNameDecapitalized = childClassName.decapitalize()
    val childClassNameSplitCamel = childClassName.splitCamel()
}

fun generateRelationStatements(): List<Relation> {
    // TODO: What about Domain and ConfigRoot classes?
    // TODO: (Their relations are extracted but we do not represent them in VRO)
    val relationsGraph = extractRelations()
    return relationsGraph.map { relationsNode ->
        relationsNode.second.map {
            Relation(
                it.name.dashedToCamelCase(),
                relationsNode.first.dashedToCamelCase(),
                it.childTypeName.dashedToCamelCase()
            )
        }
    }.flatten()
}