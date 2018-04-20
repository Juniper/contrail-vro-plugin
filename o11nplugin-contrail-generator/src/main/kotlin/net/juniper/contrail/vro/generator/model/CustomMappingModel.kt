/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClass
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.isCustomPropertyObject
import net.juniper.contrail.vro.config.isInventoryPropertyClassName

data class CustomMappingModel (
    val findableClasses: List<ClassInfoModel>,
    val rootClasses: List<ClassInfoModel>,
    val propertyClasses: List<PropertyInfo>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>,
    val propertyRelations: List<PropertyRelation>,
    val iconRootDir: String
) : GenericModel()

data class ClassInfoModel(
    val simpleName: String,
    val pluginName: String,
    val folderName: String
)

data class PropertyInfo(val simpleName: String) {
    val isPropertyAsObject : Boolean get() =
        simpleName.isCustomPropertyObject || simpleName.isInventoryPropertyClassName
}

fun Class<*>.toPropertyInfoClass() = PropertyInfo(simpleName)

fun Class<*>.toClassInfoModel() = ClassInfoModel(
    simpleName,
    pluginName,
    pluginName.folderName()
)

fun generateCustomMappingModel(
    info: ProjectInfo,
    objectClasses: List<ObjectClass>,
    rootClasses: List<ObjectClass>,
    propertyClasses: List<PropertyClass>,
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>,
    propertyRelations: List<PropertyRelation>
) = CustomMappingModel(
    objectClasses.map { it.toClassInfoModel() },
    rootClasses.map { it.toClassInfoModel() },
    propertyClasses.map { it.toPropertyInfoClass() },
    relations.map { it.toRelationModel() },
    forwardRelations,
    propertyRelations,
    info.finalProjectRoot
)
