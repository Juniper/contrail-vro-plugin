/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

data class ExecutorModel (
    val rootClasses: List<ClassInfoModel>,
    val findableClasses: List<ClassInfoModel>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>
) : GenericModel()

fun generateExecutorModel(
    objectClasses: List<ObjectClass>,
    rootClasses: List<ObjectClass>,
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>
) = ExecutorModel(
    rootClasses.map { it.toClassInfoModel() },
    objectClasses.map { it.toClassInfoModel() },
    relations.map { it.toRelationModel() },
    forwardRelations
)