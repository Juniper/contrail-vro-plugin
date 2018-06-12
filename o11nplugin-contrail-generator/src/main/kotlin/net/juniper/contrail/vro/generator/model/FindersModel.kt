/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.CategoryType
import net.juniper.contrail.vro.config.ObjectClass

data class FindersModel(
    val classes: List<ClassInfoModel>,
    val propertyRelations: List<PropertyRelation>,
    val categories: List<CategoryType>
) : GenericModel()

fun generateFindersModel(
    objectClasses: List<ObjectClass>,
    categories: List<CategoryType>,
    propertyRelations: List<PropertyRelation>
) = FindersModel(
    objectClasses.map { it.toClassInfoModel() },
    propertyRelations,
    categories
)
