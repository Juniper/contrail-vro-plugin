/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

class CustomMappingModel (
    val importPaths: List<String>,
    val canonicalNameClasses: List<Class<*>>,
    val unfindableClasses: List<Class<*>>,
    val findableClasses: List<Class<*>>,
    val rootClasses: List<Class<*>>,
    val relations: List<Relation>
) {
    class Relation(val parentClassName: String, val childClassName: String, val name: String)
}
