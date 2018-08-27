/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

@file:JvmName("Constants")

package net.juniper.contrail.vro.config.constants

val Contrail = "Contrail"
val basePackageName = "net.juniper.contrail"
val apiPackageName = "$basePackageName.api"
val apiTypesPackageName = "$basePackageName.api.types"

val VC = "VC"
val VirtualMachine = "VirtualMachine"

val Drafts = "Drafts"
val Configuration = "Configuration"
val Connection = "Connection"
val parent = "parent"
val rule = "rule"
val child = "child"
val item = "item"
val name = "name"
val element = "element"
val id = "id"
val ingress = "ingress"
val egress = "egress"
val subnet = "subnet"
val any = "any"

val tcp = "tcp"
val udp = "udp"
val icmp = "icmp"
val icmp6 = "icmp6"

val maxOtherInterfacesSupported = 8
val supportedOtherInterfaces = (0 until maxOtherInterfacesSupported).map { "other$it" }
val supportedInterfaceNames = listOf(
    "left",
    "right",
    "management"
) + supportedOtherInterfaces
val maxInterfacesSupported = supportedInterfaceNames.size

val VxLANMaxID = 16777215
val minPort = 0
val maxPort = 65535