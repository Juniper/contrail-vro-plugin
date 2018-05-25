/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config.constants

enum class EndpointType(val value: String) {
    None("none"),
    Tag("tag"),
    AddressGroup("address group"),
    VirtualNetwork("virtual network"),
    AnyWorkload("any workload")
}

enum class ServiceType(val value: String) {
    Manual("manual"),
    Reference("reference")
}