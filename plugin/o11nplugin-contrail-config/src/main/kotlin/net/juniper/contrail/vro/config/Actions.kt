/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val actionPackage = globalProjectInfo.workflowPackage
val utilActionPackage = "$actionPackage.util"

val cidrCheckingAction = "isValidCidr"
val propertyNotNull = "propertyNotNull"
val propertyValue = "propertyValue"