/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

@file:JvmName("Actions")

package net.juniper.contrail.vro.config

val actionPackage = globalProjectInfo.workflowPackage

val String.vCenterRelatedPackage get() =
    "$this.vc"

val areValidCommunityAttributes = "areValidCommunityAttributes"
val isValidAllocactionPool = "isValidAllocationPool"
val isValidCidr = "isValidCidr"
val isFreeInCidr = "isFreeInCidr"
val isInCidr = "isInCidr"
val isValidIp = "isValidIp"
val isValidVxLANId = "isValidVxLANId"
val isSingleAddressNetworkPolicyRule = "isSingleAddressNetworkPolicyRule"
val isSingleAddressSecurityGroupRule = "isSingleAddressSecurityGroupRule"
val networkOfServiceInterface = "networkOfServiceInterface"
val networkPolicyRules = "networkPolicyRules"
val serviceGroupServices = "serviceGroupServices"
val virtualNetworkSubnets = "virtualNetworkSubnets"
val networkIpamSubnets = "networkIpamSubnets"
val addressGroupSubnets = "addressGroupSubnets"
val parentConnection = "parentConnection"
val portsForServiceInterface = "portsForServiceInterface"
val isValidMac = "isValidMac"
val isValidSubnet = "isValidSubnet"
val readSubnet = "readSubnet"
val propertyNotNull = "propertyNotNull"
val propertyValue = "propertyValue"
val routeTableRoutes = "routeTableRoutes"
val serviceHasInterfaceWithName = "serviceHasInterfaceWithName"
val templateHasInterfaceWithName = "templateHasInterfaceWithName"
val serviceInstanceInterfaceNames = "serviceInstanceInterfaceNames"
val ipamHasAllocationMode = "ipamHasAllocationMode"
val ipamHasNotAllocationMode = "ipamHasNotAllocationMode"
val networkHasNotAllcationMode = "networkHasNotAllocationMode"
val listElementProperty = "listElementProperty"
val listTagTypes = "listTagTypes"
val listTagsOfType = "listTagsOfType"
val matchesSecurityScope = "matchesSecurityScope"
val defaultConnection = "defaultConnection"
val hasBackrefs = "hasBackrefs"
val isNotReferencedBy = "isNotReferencedBy"

val portOfVCVirtualMachine = "portOfVCVirtualMachine"
val networkOfVCPortGroup = "networkOfVCPortGroup"