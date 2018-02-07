/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.isAllocPoolAction
import net.juniper.contrail.vro.config.isCidrAction
import net.juniper.contrail.vro.config.isFreeInCidrAction
import net.juniper.contrail.vro.config.isInCidrAction

sealed class Validation

sealed class StringValidation : Validation()

class CIDR : StringValidation() {
    val actionName = isCidrAction
}

class AllocationPool(val cidr: String) : StringValidation() {
    val actionName = isAllocPoolAction
}

class InCIDR(val cidr: String) : StringValidation() {
    val actionName = isInCidrAction
}

class FreeInCIDR(val cidr: String, val pools: String, val dns: String) : StringValidation() {
    val actionName = isFreeInCidrAction
}