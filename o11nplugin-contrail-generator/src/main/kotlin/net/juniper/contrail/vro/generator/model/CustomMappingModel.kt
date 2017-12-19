/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.util.folderName

data class CustomMappingModel (
    val findableClassNames: List<String>,
    val rootClasses: List<ClassInfoModel>,
    val relations: List<RelationModel>,
    val referenceRelations: List<RefRelationModel>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

data class ClassInfoModel(
    val simpleName: String,
    val folderName: String
)

fun Class<*>.toClassInfoModel() = ClassInfoModel(
    simpleName,
    simpleName.folderName()
)

fun generateCustomMappingModel(
    objectClasses: List<Class<out ApiObjectBase>>,
    rootClasses: List<Class<out ApiObjectBase>>,
    relations: List<Relation>,
    referenceRelations: List<RefRelation>,
    nestedRelations: List<NestedRelation>
) = CustomMappingModel(
    objectClasses.map { it.simpleName },
    rootClasses.map { it.toClassInfoModel() },
    relations.map { it.toRelationModel() },
    referenceRelations.map { it.toRefRelationModel() },
    nestedRelations.map { it.toNestedRelationModel() }
)
