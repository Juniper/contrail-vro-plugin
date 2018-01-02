/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.util.rootClasses

data class RelationDefinition(
    val rootClasses: List<Class<out ApiObjectBase>>,
    val relations: List<Relation>,
    val referenceRelations: List<RefRelation>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

fun buildRelationDefinition(objectClasses: List<Class<out ApiObjectBase>>) = RelationDefinition(
    objectClasses.rootClasses(),
    generateRelations(objectClasses),
    generateReferenceRelations(objectClasses),
    generateNestedRelations(objectClasses)
)