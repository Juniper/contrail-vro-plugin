/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.relation.extractRelations

private fun generateRelationStatements(): List<RelationsModel.Relation> {
    // TODO: What about Domain and ConfigRoot classes?
    // TODO: (Their relations are extracted but we do not represent them in VRO)
    val relationsGraph = extractRelations()
    return relationsGraph.map { relationsNode ->
        relationsNode.second.map {
            RelationsModel.Relation(
                    dashedClassNameToCamelCase(relationsNode.first),
                    dashedClassNameToCamelCase(it.childTypeName),
                    dashedClassNameToCamelCase(it.childTypeName).decapitalize()
            )
        }
    }.flatten()
}

private fun dashedClassNameToCamelCase(name: String): String {
    return name.split("-").map { it.capitalize() }.joinToString("").replace("DNS", "Dns")
}

fun generateRelationsModel(): RelationsModel {
    val relations = generateRelationStatements()

    return RelationsModel(relations)
}