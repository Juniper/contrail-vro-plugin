/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.propertyNotNull
import net.juniper.contrail.vro.config.propertyOfObjectRule
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.readSubnet
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.boolean
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

val propertyOfObjectRule = ActionDefinition(
    name = propertyOfObjectRule,
    resultType = any,
    parameters = listOf(
        "parent" ofType any,
        "rule" ofType string,
        "propertyName" ofType string
    )
)