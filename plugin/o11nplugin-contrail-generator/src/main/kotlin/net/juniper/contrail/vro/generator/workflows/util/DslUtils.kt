/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.util

import net.juniper.contrail.vro.generator.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.generator.workflows.xsd.Schema
import net.juniper.contrail.vro.generator.workflows.xsd.propertyDescription
import net.juniper.contrail.vro.generator.workflows.xsd.relationDescription

inline fun <reified Parent : Any>
BasicParameterBuilder<*>.extractPropertyDescription(
    schema: Schema,
    convertParameterNameToXsd: Boolean = true) {
    description = schema.propertyDescription<Parent>(parameterName, convertParameterNameToXsd)
}

inline fun <reified Parent : Any, reified Child : Any>
BasicParameterBuilder<*>.extractRelationDescription(schema: Schema) {
    description = schema.relationDescription<Parent, Child>()
}