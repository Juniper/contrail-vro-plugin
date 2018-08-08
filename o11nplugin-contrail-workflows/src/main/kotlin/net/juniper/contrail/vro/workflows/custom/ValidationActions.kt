/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.areValidCommunityAttributes
import net.juniper.contrail.vro.config.isValidAllocactionPool
import net.juniper.contrail.vro.config.isValidCidr
import net.juniper.contrail.vro.config.isFreeInCidr
import net.juniper.contrail.vro.config.isInCidr
import net.juniper.contrail.vro.config.isValidIp
import net.juniper.contrail.vro.config.isValidMac
import net.juniper.contrail.vro.config.isSingleAddressNetworkPolicyRule
import net.juniper.contrail.vro.config.isSingleAddressSecurityGroupRule
import net.juniper.contrail.vro.config.isValidSubnet
import net.juniper.contrail.vro.config.matchesSecurityScope
import net.juniper.contrail.vro.config.serviceHasInterfaceWithName
import net.juniper.contrail.vro.config.templateHasInterfaceWithName
import net.juniper.contrail.vro.config.networkHasNotAllcationMode
import net.juniper.contrail.vro.config.ipamHasAllocationMode
import net.juniper.contrail.vro.config.ipamHasNotAllocationMode
import net.juniper.contrail.vro.config.isValidVxLANId
import net.juniper.contrail.vro.config.hasBackrefs
import net.juniper.contrail.vro.config.isNotReferencedBy
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

val ipValidationAction = ActionDefinition (
    name = isValidIp,
    resultType = string,
    parameters = listOf("input" ofType string)
)

val cidrValidationAction = ActionDefinition (
    name = isValidCidr,
    resultType = string,
    parameters = listOf("cidr" ofType string)
)

val macValidationAction = ActionDefinition (
    name = isValidMac,
    resultType = string,
    parameters = listOf("input" ofType string)
)

val subnetValidationAction = ActionDefinition(
    name = isValidSubnet,
    resultType = string,
    parameters = listOf("cidr" ofType string)
)

val vxlanIdValidationAction = ActionDefinition(
    name = isValidVxLANId,
    resultType = string,
    parameters = listOf("input" ofType string)
)

val allocValidationAction = ActionDefinition (
    name = isValidAllocactionPool,
    resultType = string,
    parameters = listOf("pools" ofType string.array, "cidr" ofType string)
)

val inCidrValidationAction = ActionDefinition (
    name = isInCidr,
    resultType = string,
    parameters = listOf("ip" ofType string, "cidr" ofType string)
)

val isFreeValidationAction = ActionDefinition (
    name = isFreeInCidr,
    resultType = string,
    parameters = listOf("ip" ofType string, "cidr" ofType string, "pools" ofType string.array, "dns" ofType string)
)

val isSingleAddressNetworkPolicyRuleAction = ActionDefinition (
    name = isSingleAddressNetworkPolicyRule,
    resultType = string,
    parameters = listOf("input" ofType string, "networkPolicy" ofType any)
)

val isSingleAddressSecurityGroupRuleAction = ActionDefinition (
    name = isSingleAddressSecurityGroupRule,
    resultType = string,
    parameters = listOf("input" ofType string, "securityGroup" ofType any)
)

val areValidCommunityAttributesAction = ActionDefinition (
    name = areValidCommunityAttributes,
    resultType = string,
    parameters = listOf("communityAttributes" ofType array(string))
)

val serviceHasInterfaceWithNameAction = ActionDefinition(
    name = serviceHasInterfaceWithName,
    resultType = boolean,
    parameters = listOf("serviceInstance" ofType reference<ServiceInstance>(), "name" ofType string)
)

val templateHasInterfaceWithNameAction = ActionDefinition(
    name = templateHasInterfaceWithName,
    resultType = boolean,
    parameters = listOf("serviceTemplate" ofType reference<ServiceTemplate>(), "name" ofType string)
)

val ipamHasAllocationModeAction = ActionDefinition(
    name = ipamHasAllocationMode,
    resultType = string,
    parameters = listOf("networkIpam" ofType reference<NetworkIpam>(), "mode" ofType string)
)

val ipamHasNotAllocationModeAction = ActionDefinition(
    name = ipamHasNotAllocationMode,
    resultType = string,
    parameters = listOf("networkIpam" ofType reference<NetworkIpam>(), "mode" ofType string)
)

val networkHasNotAllcationModeAction = ActionDefinition(
    name = networkHasNotAllcationMode,
    resultType = string,
    parameters = listOf("virtualNetwork" ofType reference<VirtualNetwork>(), "mode" ofType string)
)

val matchesSecurityScope = ActionDefinition(
    name = matchesSecurityScope,
    resultType = string,
    parameters = listOf(
        "children" ofType any,
        "parent" ofType any,
        "directMode" ofType boolean,
        "arrayMode" ofType boolean)
)

val hasBackrefs = ActionDefinition(
    name = hasBackrefs,
    resultType = string,
    parameters = listOf(
        "item" ofType any
    )
)

val isNotReferencedBy = ActionDefinition(
    name = isNotReferencedBy,
    resultType = string,
    parameters = listOf(
        "child" ofType any,
        "parent" ofType any
    )
)