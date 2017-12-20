/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

data class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<RelationModel>,
    val referenceRelations: List<RefRelationModel>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

data class RelationModel(
    val parentName: String,
    val childName: String,
    val childNameDecapitalized: String,
    val name: String,
    val folderName: String
)

data class RefRelationModel(
    val parentName: String,
    val childName: String,
    val childOriginalName: String,
    val getter: String,
    val referenceAttribute: String,
    val simpleReference: Boolean,
    val backReference: Boolean,
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

fun RefRelation.toRefRelationModel() = RefRelationModel(
    parentName,
    childName,
    childOriginalName,
    getter,
    referenceAttribute.simpleName,
    simpleReference,
    backReference,
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
    refRelations: List<RefRelation>,
    nestedRelations: List<NestedRelation>,
    rootClasses: List<Class<*>>
): RelationsModel {
    val relationModels = relations.map { it.toRelationModel() }
    val refRelationModels = refRelations.map { it.toRefRelationModel() }
    val nestedRelationModels = nestedRelations.map { it.toNestedRelationModel() }
    val rootClassNames = rootClasses.map { it.simpleName }

    return RelationsModel(rootClassNames, relationModels, refRelationModels, nestedRelationModels)
}
