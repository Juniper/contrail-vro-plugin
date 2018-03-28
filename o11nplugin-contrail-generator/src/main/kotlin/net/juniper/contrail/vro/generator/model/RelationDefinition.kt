/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.isInventoryProperty

class RelationDefinition(
    val modelClasses: List<ObjectClass>,
    val relations: List<Relation>,
    val forwardRelations: List<ForwardRelation>,
    val propertyRelations: List<PropertyRelation>
) : GenericModel()

fun buildRelationDefinition(
    objectClasses: List<ObjectClass>
) = RelationDefinition(
    objectClasses.toList(),
    objectClasses.generateRelations(),
    objectClasses.generateReferenceRelations(),
    objectClasses.generatePropertyRelations { it.isInventoryProperty }
)