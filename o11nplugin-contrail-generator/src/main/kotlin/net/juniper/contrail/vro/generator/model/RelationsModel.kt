/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.isDirectChild
import net.juniper.contrail.vro.config.pluralize
import net.juniper.contrail.vro.config.toPluginName

data class RelationsModel(
    val rootClasses: List<ClassInfoModel>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>,
    val propertyRelations: List<PropertyRelation>,
    val categories: List<Category>,
    val securityClasses: List<ClassInfoModel>
) : GenericModel()

data class RelationModel(
    val parentName: String,
    val childName: String,
    val isDirectChild: Boolean,
    val folderName: String
) {
    val childNameDecapitalized = childName.decapitalize()
    val parentPluginName = parentName.toPluginName
    val childPluginName = childName.toPluginName
    val childPluginNameDecapitalized = childPluginName.decapitalize()
    val childPluginNamePluralized = childPluginName.pluralize()
    val childPluginNamePluralizedDecapitalized = childPluginNamePluralized.decapitalize()
    val getter: String = childName.decapitalize() + "s"
}

fun Relation.toRelationModel() = RelationModel(
    parentName,
    childName,
    childName.isDirectChild,
    folderName
)

fun generateRelationsModel(
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>,
    propertyRelations: List<PropertyRelation>,
    rootClasses: List<ObjectClass>
): RelationsModel {
    val relationModels = relations.map { it.toRelationModel() }
    val categories = relations.toCategoryList()
    val rootClassesModel = rootClasses.map { it.toClassInfoModel() }
    val securityClasses = relations.toSecurityClasses()

    return RelationsModel(rootClassesModel, relationModels, forwardRelations, propertyRelations, categories, securityClasses)
}
