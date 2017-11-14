/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<Relation>
) {
    class Relation(val parentClassName: String, val childClassName: String, val childClassNameDecapitalized: String)
}