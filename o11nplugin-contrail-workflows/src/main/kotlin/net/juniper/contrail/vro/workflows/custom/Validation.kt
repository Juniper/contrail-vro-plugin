/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.isAllocPoolAction
import net.juniper.contrail.vro.config.isCidrAction
import net.juniper.contrail.vro.config.isFreeInCidrAction
import net.juniper.contrail.vro.config.isInCidrAction
import net.juniper.contrail.vro.config.isIpAction
import net.juniper.contrail.vro.config.isSingleAddressNetworkPolicyRuleAction
import net.juniper.contrail.vro.config.isSingleAddressSecurityGroupRuleAction
import net.juniper.contrail.vro.config.areValidCommunityAttributes
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder

fun BasicParameterBuilder<String>.isCidr() =
    validationActionCallTo(isCidrAction)

fun BasicParameterBuilder<String>.isIPAddress() =
    validationActionCallTo(isIpAction)

fun BasicParameterBuilder<String>.addressIsFreeInSubnet(subnet: String, allocationPools: String, dnsServerAddress: String) =
    validationActionCallTo(isFreeInCidrAction).parameters(subnet, allocationPools, dnsServerAddress)

fun BasicParameterBuilder<String>.addressInSubnet(subnet: String) =
    validationActionCallTo(isInCidrAction).parameter(subnet)

fun BasicParameterBuilder<List<String>>.allocationPoolInSubnet(subnet: String) =
    validationActionCallTo(isAllocPoolAction).parameter(subnet)

fun BasicParameterBuilder<String>.isSingleAddressNetworkPolicyRuleOf(networkPolicy: String) =
    validationActionCallTo(isSingleAddressNetworkPolicyRuleAction).parameter(networkPolicy)

fun BasicParameterBuilder<String>.isSingleAddressSecurityGroupRuleOf(securityGroup: String) =
    validationActionCallTo(isSingleAddressSecurityGroupRuleAction).parameter(securityGroup)

fun BasicParameterBuilder<List<String>>.isCommunityAttribute() =
    validationActionCallTo(areValidCommunityAttributes)
