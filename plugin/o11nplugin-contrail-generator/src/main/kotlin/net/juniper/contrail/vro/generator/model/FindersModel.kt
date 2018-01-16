/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass

data class FindersModel(
    val classNames: List<String>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

fun generateFindersModel(
    objectClasses: List<ObjectClass>,
    nestedRelations: List<NestedRelation>
) = FindersModel(
    objectClasses.map { it.simpleName },
    nestedRelations.map { it.toNestedRelationModel() }
)
