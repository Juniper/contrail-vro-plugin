/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<Relation>,
    val referenceRelations: List<RefRelation>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

fun generateRelationsModel(
    objectClasses: List<Class<out ApiObjectBase>>
): RelationsModel {
    val relations = generateRelations(objectClasses)
    val refRelations = generateReferenceRelations(objectClasses)
    val nestedRelations = generateNestedRelations(objectClasses)
    val rootClassNames = objectClasses.rootClasses()
        .map { it.simpleName }

    return RelationsModel(rootClassNames, relations, refRelations, nestedRelations)
}
