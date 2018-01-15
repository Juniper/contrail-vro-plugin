/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClassFilter

class RelationDefinition(
    val rootClasses: List<ObjectClass>,
    val relations: List<Relation>,
    val forwardRelations: List<ForwardRelation>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

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