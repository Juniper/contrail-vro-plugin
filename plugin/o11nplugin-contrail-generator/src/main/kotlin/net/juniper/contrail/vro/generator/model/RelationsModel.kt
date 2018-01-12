/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

data class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

data class RelationModel(
    val parentName: String,
    val childName: String,
    val childNameDecapitalized: String,
    val name: String,
    val folderName: String
)

data class NestedRelationModel(
    val childWrapperName: String,
    val parentWrapperName: String,
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
    childName.decapitalize(),
    name,
    folderName
)

fun NestedRelation.toNestedRelationModel() = NestedRelationModel(
    childWrapperName,
    parentWrapperName,
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
    val rootClassNames = rootClasses.map { it.simpleName }

    return RelationsModel(rootClassNames, relationModels, forwardRelations, nestedRelationModels)
}
