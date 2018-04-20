/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClassFilter
import net.juniper.contrail.vro.config.rootClassFilter

class RelationDefinition(
    val rootClasses: List<ObjectClass>,
    val relations: List<Relation>,
    val forwardRelations: List<ForwardRelation>,
    val propertyRelations: List<PropertyRelation>
) : GenericModel()

fun buildRelationDefinition(
    objectClasses: List<ObjectClass>,
    inventoryPropertyFilter: PropertyClassFilter
) = RelationDefinition(
    objectClasses.filter(rootClassFilter),
    objectClasses.generateRelations(),
    objectClasses.generateReferenceRelations(),
    objectClasses.generatePropertyRelations(inventoryPropertyFilter)
)