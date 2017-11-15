/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<Relation>
)

fun generateRelationsModel(): RelationsModel {
    val relations = generateRelationStatements()
    val rootClassNames = rootClasses()
        .map { it.simpleName }

    return RelationsModel(rootClassNames, relations)
}