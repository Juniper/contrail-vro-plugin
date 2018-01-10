/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

data class FindersModel(
    val classNames: List<String>,
    val referenceWrappers: List<ReferenceWrapperModel>,
    val nestedRelations: List<NestedRelationModel>
) : GenericModel()

fun generateFindersModel(
    objectClasses: List<ObjectClass>,
    referenceWrappers: List<ReferenceWrapper>,
    nestedRelations: List<NestedRelation>
) = FindersModel(
    objectClasses.map { it.simpleName },
    referenceWrappers.map { it.toReferenceWrapperModel() },
    nestedRelations.map { it.toNestedRelationModel() }
)
