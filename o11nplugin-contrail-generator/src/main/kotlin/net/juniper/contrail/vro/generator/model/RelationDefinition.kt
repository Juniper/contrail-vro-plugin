/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.generator.util.select

class RelationDefinition(
    val rootClasses: List<ObjectClass>,
    val relations: List<Relation>,
    referenceRelations: List<RefRelation>,
    val nestedRelations: List<NestedRelation>
) : GenericModel() {
    val forwardRelations = referenceRelations.select(ForwardRelation::class.java)
    val backwardRelations = referenceRelations.select(BackwardRelation::class.java)
}

fun buildRelationDefinition(
    objectClasses: List<ObjectClass>,
    rootClasses: List<ObjectClass>,
    inventoryPropertyFilter: PropertyClassFilter
) = RelationDefinition(
    rootClasses,
    objectClasses.generateRelations(),
    objectClasses.generateReferenceRelations(),
    objectClasses.generateNestedRelations(inventoryPropertyFilter)
)