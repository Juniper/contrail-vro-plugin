/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.isValidAllocactionPool
import net.juniper.contrail.vro.config.isValidCidr
import net.juniper.contrail.vro.config.isValidSubnet
import net.juniper.contrail.vro.config.isValidMac
import net.juniper.contrail.vro.config.isFreeInCidr
import net.juniper.contrail.vro.config.isInCidr
import net.juniper.contrail.vro.config.isValidIp
import net.juniper.contrail.vro.config.isSingleAddressNetworkPolicyRule
import net.juniper.contrail.vro.config.isSingleAddressSecurityGroupRule
import net.juniper.contrail.vro.config.areValidCommunityAttributes
import net.juniper.contrail.vro.config.networkHasNotAllcationMode
import net.juniper.contrail.vro.config.ipamHasAllcationMode
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.dsl.ReferenceParameterBuilder

fun BasicParameterBuilder<String>.isSubnet() =
    validationActionCallTo(isValidSubnet)

fun ReferenceParameterBuilder.networkHasNotAllocationMode(mode: String) =
    validationActionCallTo(networkHasNotAllcationMode).string(mode)

fun ReferenceParameterBuilder.ipamHasAllocationMode(mode: String, expected: Boolean) =
    validationActionCallTo(ipamHasAllcationMode).string(mode).boolean(expected)

fun BasicParameterBuilder<String>.isCidr() =
    validationActionCallTo(isValidCidr)

fun BasicParameterBuilder<String>.isMac() =
    validationActionCallTo(isValidMac)

fun BasicParameterBuilder<String>.isIPAddress() =
    validationActionCallTo(isValidIp)

fun BasicParameterBuilder<String>.addressIsFreeInSubnet(subnet: String, allocationPools: String, dnsServerAddress: String) =
    validationActionCallTo(isFreeInCidr).parameters(subnet, allocationPools, dnsServerAddress)

fun BasicParameterBuilder<String>.addressInSubnet(subnet: String) =
    validationActionCallTo(isInCidr).parameter(subnet)

fun BasicParameterBuilder<List<String>>.allocationPoolInSubnet(subnet: String) =
    validationActionCallTo(isValidAllocactionPool).parameter(subnet)

fun BasicParameterBuilder<String>.isSingleAddressNetworkPolicyRuleOf(networkPolicy: String) =
    validationActionCallTo(isSingleAddressNetworkPolicyRule).parameter(networkPolicy)

fun BasicParameterBuilder<String>.isSingleAddressSecurityGroupRuleOf(securityGroup: String) =
    validationActionCallTo(isSingleAddressSecurityGroupRule).parameter(securityGroup)

fun BasicParameterBuilder<List<String>>.isCommunityAttribute() =
    validationActionCallTo(areValidCommunityAttributes)