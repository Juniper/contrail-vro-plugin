/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass

data class FindersModel(
    val classes: List<ClassInfoModel>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

fun generateFindersModel(
    objectClasses: List<ObjectClass>,
    nestedRelations: List<NestedRelation>
) = FindersModel(
    objectClasses.map { it.toClassInfoModel() },
    nestedRelations.map { it.toNestedRelationModel() }
)
