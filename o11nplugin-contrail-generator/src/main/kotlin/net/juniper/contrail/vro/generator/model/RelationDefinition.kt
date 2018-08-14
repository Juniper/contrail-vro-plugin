/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ObjectClass

class RelationDefinition(
    val modelClasses: List<ObjectClass>,
    val relations: List<Relation>,
    val forwardRelations: List<ForwardRelation>,
    val propertyRelations: List<PropertyRelation>
) : GenericModel()

fun buildRelationDefinition(
    objectClasses: List<ObjectClass>,
    config: Config
) = RelationDefinition(
    objectClasses.toList(),
    objectClasses.generateRelations(config),
    objectClasses.generateReferenceRelations(config),
    objectClasses.generatePropertyRelations { config.isInventoryProperty(it) }
)