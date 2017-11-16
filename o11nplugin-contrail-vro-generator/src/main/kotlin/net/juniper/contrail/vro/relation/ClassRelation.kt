/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.relation

data class ClassRelation(val name: String, val childTypeName: String)

fun buildRelation(parentType: String, childType: String ): ClassRelation {
    val relationName = "${parentType}To$childType"
    return ClassRelation(relationName, childType)
}
