/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.generator.util.folderName

data class CustomMappingModel (
    val findableClassNames: List<String>,
    val rootClasses: List<ClassInfoModel>,
    val propertyClassNames: List<String>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>,
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
    objectClasses: List<ObjectClass>,
    rootClasses: List<ObjectClass>,
    propertyClasses: List<PropertyClass>,
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>,
    nestedRelations: List<NestedRelation>
) = CustomMappingModel(
    objectClasses.map { it.simpleName },
    rootClasses.map { it.toClassInfoModel() },
    propertyClasses.map { it.simpleName },
    relations.map { it.toRelationModel() },
    forwardRelations,
    nestedRelations.map { it.toNestedRelationModel() }
)
