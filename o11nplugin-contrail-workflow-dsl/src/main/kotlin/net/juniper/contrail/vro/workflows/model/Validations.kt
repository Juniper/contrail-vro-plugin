/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.isAllocPoolAction
import net.juniper.contrail.vro.config.isCidrAction
import net.juniper.contrail.vro.config.isFreeInCidrAction
import net.juniper.contrail.vro.config.isInCidrAction
import net.juniper.contrail.vro.config.isMultiAddressNetworkPolicyRuleAction
import net.juniper.contrail.vro.config.isMultiAddressSecurityGroupRuleAction

sealed class Validation

sealed class StringValidation : Validation()
sealed class ArrayValidation : Validation()

class CIDR : StringValidation() {
    val actionName = isCidrAction
}

class AllocationPool(val cidr: String) : ArrayValidation() {
    val actionName = isAllocPoolAction
}

class InCIDR(val cidr: String) : StringValidation() {
    val actionName = isInCidrAction
}

class FreeInCIDR(val cidr: String, val pools: String, val dns: String) : StringValidation() {
    val actionName = isFreeInCidrAction
}

class MultiAddressNetworkPolicyRule(val policyFieldName: String) : StringValidation() {
    val actionName = isMultiAddressNetworkPolicyRuleAction
}

class MultiAddressSecurityGroupRule(val securityGroupFieldName: String) : StringValidation() {
    val actionName = isMultiAddressSecurityGroupRuleAction
}