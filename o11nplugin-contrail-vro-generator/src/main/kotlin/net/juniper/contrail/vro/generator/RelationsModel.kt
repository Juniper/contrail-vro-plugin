/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<Relation>
) : GenericModel()

fun generateRelationsModel(objectClasses: List<Class<out ApiObjectBase>>): RelationsModel {
    val relations = generateRelationStatements(objectClasses)
    val rootClassNames = rootClasses(objectClasses)
        .map { it.simpleName }

    return RelationsModel(rootClassNames, relations)
}
