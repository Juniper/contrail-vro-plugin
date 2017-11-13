/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

class RelationsModel(
    val relations: List<Relation>
) {
    class Relation(val parentClassName: String, val childClassName: String, val childClassNameDecapitalized: String)
}