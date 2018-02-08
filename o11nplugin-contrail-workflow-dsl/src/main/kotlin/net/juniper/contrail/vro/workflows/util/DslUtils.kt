/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.util

import net.juniper.contrail.vro.config.toTitle
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.relationDescription

inline fun <reified Parent : Any>
BasicParameterBuilder<*>.extractPropertyDescription(
    schema: Schema,
    convertParameterNameToXsd: Boolean = true,
    title: String = parameterName.toTitle()) {
    description = """
        $title
        ${schema.propertyDescription<Parent>(parameterName, convertParameterNameToXsd)}
        """.trimIndent()
}

inline fun <reified Parent : Any, reified Child : Any>
BasicParameterBuilder<*>.extractRelationDescription(schema: Schema) {
    description = schema.relationDescription<Parent, Child>()
}