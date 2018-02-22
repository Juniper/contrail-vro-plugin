/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val actionPackage = globalProjectInfo.workflowPackage
val utilActionPackage = "$actionPackage.util"

val parentConnection = "parentConnection"
val isCidrAction = "isValidCidr"
val isIpAction = "isValidIp"
val propertyNotNull = "propertyNotNull"
val propertyValue = "propertyValue"
val isInCidrAction = "isInCidr"
val isFreeInCidrAction = "isFreeInCidr"
val isAllocPoolAction = "isValidAllocationPool"
val isSingleAddressNetworkPolicyRuleAction = "isSingleAddressNetworkPolicyRule"
val isSingleAddressSecurityGroupRuleAction = "isSingleAddressSecurityGroupRule"
val extractListProperty = "getListPropertyValue"
val getNetworkPolicyRules = "getNetworkPolicyRules"
val getSubnetsOfVirtualNetwork = "getSubnetsOfVirtualNetwork"
