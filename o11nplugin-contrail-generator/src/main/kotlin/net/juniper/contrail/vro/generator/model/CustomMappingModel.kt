/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClass
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.pluginName

data class CustomMappingModel (
    val findableClasses: List<ClassInfoModel>,
    val rootClasses: List<ClassInfoModel>,
    val securityClasses: List<ClassInfoModel>,
    val propertyClasses: List<PropertyInfo>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>,
    val propertyRelations: List<PropertyRelation>,
    val categories: List<Category>,
    val categoryRelations: List<CategoryRelation>,
    val iconRootDir: String
) : GenericModel()

data class ClassInfoModel(
    val simpleName: String,
    val pluginName: String
) {
    val folderName = pluginName.folderName()
    val simpleNameDecapitalized = simpleName.decapitalize()
}

data class PropertyInfo(val simpleName: String, val config: Config) {
    val isPropertyAsObject : Boolean get() =
        config.isCustomPropertyObject(simpleName) || config.isInventoryPropertyClassName(simpleName)
}

fun Class<*>.toPropertyInfoClass(config: Config) = PropertyInfo(simpleName, config)

fun Class<*>.toClassInfoModel() = ClassInfoModel(
    simpleName,
    pluginName
)

fun generateCustomMappingModel(
    info: ProjectInfo,
    pluginClasses: List<ObjectClass>,
    rootClasses: List<ObjectClass>,
    propertyClasses: List<PropertyClass>,
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>,
    propertyRelations: List<PropertyRelation>,
    config: Config
) = CustomMappingModel(
    pluginClasses.map { it.toClassInfoModel() },
    rootClasses.map { it.toClassInfoModel() },
    relations.toSecurityClasses().toList(),
    propertyClasses.map { it.toPropertyInfoClass(config) },
    relations.filter { !it.isCategoryRelation }.map { it.toRelationModel(config) },
    forwardRelations,
    propertyRelations,
    relations.toCategories().toList(),
    relations.toCategoryRelations().toList(),
    info.finalProjectRoot
)
