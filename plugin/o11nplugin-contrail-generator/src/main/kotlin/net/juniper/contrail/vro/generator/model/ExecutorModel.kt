/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass

data class ExecutorModel (
    val rootClasses: List<ClassInfoModel>,
    val findableClasses: List<ClassInfoModel>,
    val internalClasses: List<ClassInfoModel>,
    val relations: List<RelationModel>,
    val forwardRelations: List<ForwardRelation>
) : GenericModel()

fun generateExecutorModel(
    objectClasses: List<ObjectClass>,
    rootClasses: List<ObjectClass>,
    internalClasses: List<ObjectClass>,
    relations: List<Relation>,
    forwardRelations: List<ForwardRelation>
) = ExecutorModel(
    rootClasses.map { it.toClassInfoModel() },
    objectClasses.map { it.toClassInfoModel() },
    internalClasses.map { it.toClassInfoModel() },
    relations.map { it.toRelationModel() },
    forwardRelations
)