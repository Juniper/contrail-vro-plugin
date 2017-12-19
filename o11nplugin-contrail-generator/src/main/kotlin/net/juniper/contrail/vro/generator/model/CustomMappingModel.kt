/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.generator.util.folderName

data class CustomMappingModel (
    val findableClassNames: List<String>,
    val rootClasses: List<ClassInfoModel>,
    val propertyClassNames: List<String>,
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
    propertyClasses: List<Class<out ApiPropertyBase>>,
    relations: List<Relation>,
    referenceRelations: List<RefRelation>,
    nestedRelations: List<NestedRelation>
) = CustomMappingModel(
    objectClasses.map { it.simpleName },
    rootClasses.map { it.toClassInfoModel() },
    propertyClasses.map { it.simpleName },
    relations.map { it.toRelationModel() },
    referenceRelations.map { it.toRefRelationModel() },
    nestedRelations.map { it.toNestedRelationModel() }
)
