/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.vCenterRelatedPackage
import net.juniper.contrail.vro.workflows.model.Action

fun loadCustomActions(version: String, packageName: String): List<Action> = mutableListOf<Action>().apply {
    this += cidrValidationAction(version, packageName)
    this += serviceInstanceInterfaceNamesAction(version, packageName)
    this += allocValidationAction(version, packageName)
    this += areValidCommunityAttributesAction(version, packageName)
    this += macValidationAction(version, packageName)
    this += subnetValidationAction(version, packageName)
    this += readSubnetAction(version, packageName)
    this += networkOfServiceInterfaceAction(version, packageName)
    this += networkPolicyRulesAction(version, packageName)
    this += portsForServiceInterfaceAction(version, packageName)
    this += routeTableRoutesAction(version, packageName)
    this += subnetsOfVirtualNetworkAction(version, packageName)
    this += inCidrValidationAction(version, packageName)
    this += ipValidationAction(version, packageName)
    this += vxlanIdValidationAction(version, packageName)
    this += isFreeValidationAction(version, packageName)
    this += isSingleAddressNetworkPolicyRuleAction(version, packageName)
    this += isSingleAddressSecurityGroupRuleAction(version, packageName)
    this += parentConnectionAction(version, packageName)
    this += propertyNotNullAction(version, packageName)
    this += propertyRetrievalAction(version, packageName)
    this += serviceHasInterfaceWithNameAction(version, packageName)
    this += templateHasInterfaceWithNameAction(version, packageName)
    this += networkIpamSubnets(version, packageName)
    this += ipamHasAllocationModeAction(version, packageName)
    this += ipamHasNotAllocationModeAction(version, packageName)
    this += networkHasNotAllcationModeAction(version, packageName)
    this += propertyOfObjectRule(version, packageName)

    this += portOfVCVirtualMachineAction(version, packageName.vCenterRelatedPackage)
}