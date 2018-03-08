/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.model.Action

fun loadCustomActions(version: String, packageName: String): List<Action> = mutableListOf<Action>().apply {
    this += propertyRetrievalAction(version, packageName)
    this += propertyNotNullAction(version, packageName)
    this += parentConnectionAction(version, packageName)
    this += cidrValidationAction(version, packageName)
    this += ipValidationAction(version, packageName)
    this += extractListPropertyAction(version, packageName)
    this += getNetworkPolicyRulesAction(version, packageName)
    this += getNetworkOfServiceInterface(version, packageName)
    this += serviceHasInterfaceWithName(version, packageName)
    this += templateHasInterfaceWithName(version, packageName)
    this += getPortsForServiceInterface(version, packageName)
    this += getSubnetsOfVirtualNetworkAction(version, packageName)
    this += allocValidationAction(version, packageName)
    this += inCidrValidationAction(version, packageName)
    this += isFreeValidationAction(version, packageName)
    this += isMultiAddressNetworkPolicyRuleAction(version, packageName)
    this += isMultiAddressSecurityGroupRuleAction(version, packageName)
    this += areValidCommunityAttributes(version, packageName)
}