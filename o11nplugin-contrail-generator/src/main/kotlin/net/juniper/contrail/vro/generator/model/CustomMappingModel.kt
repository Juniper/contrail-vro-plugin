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

data class PropertyInfo(val simpleName: String) {
    val isPropertyAsObject : Boolean get() =
        simpleName.isCustomPropertyObject || simpleName.isInventoryPropertyClassName
}

fun Class<*>.toPropertyInfoClass() = PropertyInfo(simpleName)

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
    propertyRelations: List<PropertyRelation>
) = CustomMappingModel(
    pluginClasses.map { it.toClassInfoModel() },
    rootClasses.map { it.toClassInfoModel() },
    relations.toSecurityClasses(),
    propertyClasses.map { it.toPropertyInfoClass() },
    relations.filter { !it.isCategoryRelation }.map { it.toRelationModel() },
    forwardRelations,
    propertyRelations,
    relations.toCategoryList(),
    relations.toCategoryRelationList(),
    info.finalProjectRoot
)
