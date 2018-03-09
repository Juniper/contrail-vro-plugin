/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass

data class ExecutorModel (
    val findableClasses: List<ClassInfoModel>
) : GenericModel()

fun generateExecutorModel(
    objectClasses: List<ObjectClass>
) = ExecutorModel(
    objectClasses.map { it.toClassInfoModel() }
)