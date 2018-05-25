/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config.constants

val Contrail = "Contrail"
val basePackageName = "net.juniper.contrail"
val apiPackageName = "$basePackageName.api"
val apiTypesPackageName = "$basePackageName.api.types"

val VC = "VC"
val VirtualMachine = "VirtualMachine"

val Configuration = "Configuration"
val Connection = "Connection"
val parent = "parent"
val child = "child"
val item = "item"
val id = "id"
val ingress = "ingress"
val egress = "egress"

val maxOtherInterfacesSupported = 8
val supportedOtherInterfaces = (0 until maxOtherInterfacesSupported).map { "other$it" }
val supportedInterfaceNames = listOf(
    "left",
    "right",
    "management"
) + supportedOtherInterfaces
val maxInterfacesSupported = supportedInterfaceNames.size

val VxLANMaxID = 16777215

enum class EndpointType(val value: String) {
    None("none"),
    Tag("tag"),
    AddressGroup("addressGroup"),
    VirtualNetwork("virtualNetwork"),
    AnyWorkload("anyWorkload")
}

enum class ServiceType(val value: String) {
    None("none"),
    Manual("manual"),
    Reference("reference")
}