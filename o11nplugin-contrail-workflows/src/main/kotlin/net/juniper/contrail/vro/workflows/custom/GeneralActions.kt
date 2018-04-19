/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.config.listPropertyValue
import net.juniper.contrail.vro.config.propertyNotNull
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.readSubnet
import net.juniper.contrail.vro.config.networkPolicyRuleAddressType
import net.juniper.contrail.vro.config.securityGroupRuleAddressType
import net.juniper.contrail.vro.config.propertyOfRuleOfPolicy
import net.juniper.contrail.vro.config.propertyOfAddressOfRuleOfPolicy
import net.juniper.contrail.vro.config.propertyOfAddressOfRuleOfSecurityGroup
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

private val item = "item"
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

/** listProperty
 *
 * Action retrieves nested property that is located inside a list
 *
 * @param parentItem : Any - any inventory object
 * @param childItem : String - a human-readable representation of the object with it's index at the beginning
 * @param listAccessor: String - path to the list property
 * @param propertyPath: String - path to the final property
 */
val listPropertyAction = ActionDefinition (
    name = listPropertyValue,
    resultType = any,
    parameters = listOf(
        "parentItem" ofType any,
        "childItem" ofType string,
        "listAccessor" ofType string,
        "propertyPath" ofType string)
)

val readSubnetAction = ActionDefinition (
    name = readSubnet,
    resultType = string,
    parameters = listOf(
        item ofType any,
        parameterPath ofType string
    )
)

val networkPolicyRuleAddressType = ActionDefinition(
    name = networkPolicyRuleAddressType,
    resultType = string,
    parameters = listOf(
        "networkPolicy" ofType reference<NetworkPolicy>(),
        "rule" ofType string,
        "dstMode" ofType boolean
    )
)

val securityGroupRuleAddressType = ActionDefinition(
    name = securityGroupRuleAddressType,
    resultType = string,
    parameters = listOf(
        "securityGroup" ofType reference<SecurityGroup>(),
        "rule" ofType string,
        "dstMode" ofType boolean
    )
)

val propertyOfRuleOfPolicy = ActionDefinition(
    name = propertyOfRuleOfPolicy,
    resultType = any,
    parameters = listOf(
        "networkPolicy" ofType reference<NetworkPolicy>(),
        "rule" ofType string,
        "propertyName" ofType string
    )
)

val propertyOfAddressOfRuleOfPolicy = ActionDefinition(
    name = propertyOfAddressOfRuleOfPolicy,
    resultType = any,
    parameters = listOf(
        "networkPolicy" ofType reference<NetworkPolicy>(),
        "rule" ofType string,
        "dstMode" ofType boolean,
        "propertyName" ofType string
    )
)

val propertyOfAddressOfRuleOfSecurityGroup = ActionDefinition(
    name = propertyOfAddressOfRuleOfSecurityGroup,
    resultType = any,
    parameters = listOf(
        "securityGroup" ofType reference<SecurityGroup>(),
        "rule" ofType string,
        "dstMode" ofType boolean,
        "propertyName" ofType string
    )
)