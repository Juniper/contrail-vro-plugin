/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.isDirectChild
import net.juniper.contrail.vro.config.toPluginName

data class RelationsModel(
    val rootClasses: List<ClassInfoModel>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

data class RelationModel(
    val parentName: String,
    val childName: String,
    val parentPluginName: String,
    val childPluginName: String,
    val childNameDecapitalized: String,
    val name: String,
    val isDirectChild: Boolean,
    val folderName: String
)

data class NestedRelationModel(
    val childWrapperName: String,
    val parentWrapperName: String,
    val childName: String,
    val name: String,
    val getter: String,
    val folderName: String,
    val toMany: Boolean,
    val rootClassSimpleName: String,
    val getterChain: List<GetterModel>
)

data class GetterModel(
    val name: String,
    val nameDecapitalized: String,
    val toMany: Boolean
)

fun Relation.toRelationModel() = RelationModel(
    parentName,
    childName,
    parentName.toPluginName,
    childName.toPluginName,
    childName.decapitalize(),
    name,
    childName.isDirectChild,
    folderName
)

fun NestedRelation.toNestedRelationModel() = NestedRelationModel(
    childWrapperName,
    parentWrapperName,
    child.simpleName,
    name,
    getter,
    folderName,
    toMany,
    rootClass.simpleName,
    getterChain.map { it.toGetterModel() }
)

fun Getter.toGetterModel() = GetterModel(
    name,
    name.decapitalize(),
    toMany
)

fun generateRelationsModel(
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>,
    nestedRelations: List<NestedRelation>,
    rootClasses: List<ObjectClass>
): RelationsModel {
    val relationModels = relations.map { it.toRelationModel() }
    val nestedRelationModels = nestedRelations.map { it.toNestedRelationModel() }
    val rootClassesModel = rootClasses.map { it.toClassInfoModel() }

    return RelationsModel(rootClassesModel, relationModels, forwardRelations, nestedRelationModels)
}
