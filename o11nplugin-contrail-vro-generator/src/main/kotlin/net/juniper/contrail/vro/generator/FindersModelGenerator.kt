/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.relation.nonAbstractSubclassesIn

fun generateFindersModel(): FindersModel {
    val objectClasses = ApiObjectBase::class.java.nonAbstractSubclassesIn("net.juniper.contrail.api")
    return FindersModel(objectClasses)
}