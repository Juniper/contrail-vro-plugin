/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.constants.element
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.listTagTypes
import net.juniper.contrail.vro.config.propertyNotNull
import net.juniper.contrail.vro.config.listTagsOfType
import net.juniper.contrail.vro.config.listElementProperty
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.readSubnet
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

private val parameterPath = "parameterPath"

val propertyRetrievalAction = ActionDefinition (
    name = propertyValue,
    resultType = any,
    parameters = listOf(
        item ofType any,
        parameterPath ofType string
    )
)

val propertyNotNullAction = ActionDefinition (
    name = propertyNotNull,
    resultType = boolean,
    parameters = listOf(
        item ofType any,
        parameterPath ofType string
    )
)

val readSubnetAction = ActionDefinition (
    name = readSubnet,
    resultType = string,
    parameters = listOf(
        item ofType any,
        parameterPath ofType string
    )
)

val listElementPropertyAction = ActionDefinition(
    name = listElementProperty,
    resultType = any,
    parameters = listOf(
        item ofType any,
        element ofType string,
        "propertyPrefix" ofType string,
        "propertyName" ofType string
    )
)

val listTagTypesAction = ActionDefinition (
    name = listTagTypes,
    resultType = string.array,
    parameters = listOf(
        item ofType any
    )
)

val listTagsOfType = ActionDefinition (
    name = listTagsOfType,
    resultType = reference<Tag>().array,
    parameters = listOf(
        item ofType any,
        "tagType" ofType string
    )
)