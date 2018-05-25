/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config.constants

enum class EndpointType(val value: String) {
    None("none"),
    Tag("tag"),
    AddressGroup("addressGroup"),
    VirtualNetwork("virtualNetwork"),
    AnyWorkload("anyWorkload")
}

enum class ServiceType(val value: String) {
    Manual("manual"),
    Reference("reference")
}