/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.relation.extractRelations

class Relation (
    val name: String,
    val parentClassName: String,
    val childClassName: String
) {
    val childClassNameDecapitalized = childClassName.decapitalize()
    val childClassNameSplitCamel = childClassName.splitCamel()
}

fun generateRelationStatements(classes: List<Class<out ApiObjectBase>>): List<Relation> {
    val relationsGraph = extractRelations(classes)
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
