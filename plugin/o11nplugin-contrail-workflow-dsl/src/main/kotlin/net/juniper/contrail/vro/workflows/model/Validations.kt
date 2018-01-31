/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

sealed class Validation

sealed class StringValidation : Validation()

class CIDR : StringValidation() {
    val actionPackageName = "net.juniper.contrail"
    val actionName = "isValidCidr"
}