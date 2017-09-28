/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.relation

enum class Cardinality(val value: String) {
    ONE_TO_ONE("to-one"),
    ONE_TO_MANY("to-many");

    val isOneToOne get() =
        ONE_TO_ONE == this
    val isOneToMany get() =
        ONE_TO_MANY == this
}

data class Relation(
    val name: String,
    val childTypeName: String,
    val cardinality: Cardinality = Cardinality.ONE_TO_MANY)

fun buildRelation(
        parentType: String,
        childType: String,
        cardinality: Cardinality = Cardinality.ONE_TO_MANY
): Relation {
    val relationName = "$parentType-to-$childType"
    return Relation(relationName, childType, cardinality)
}
