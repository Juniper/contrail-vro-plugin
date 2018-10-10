/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.ServiceHealthCheck
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork

sealed class CategoryType {
    val name: String get() =
        javaClass.simpleName
}

object Networking : CategoryType()
object Services : CategoryType()
object Security : CategoryType()

object Configuration {
    val name = "Configuration"
}

// project-level draft security object
object DraftSecurity {
    val name = "Draft Security"
}

// top level security object
object GlobalSecurity {
    val name = Security.name
}

// top level draft security object
object GlobalDraftSecurity {
    val name = DraftSecurity.name
}

val Class<*>.category get() = when (this) {
    VirtualNetwork::class.java,
    VirtualMachineInterface::class.java,
    NetworkIpam::class.java,
    FloatingIpPool::class.java,
    FloatingIp::class.java,
    NetworkPolicy::class.java,
    SecurityGroup::class.java -> Networking

    ServiceTemplate::class.java,
    ServiceInstance::class.java,
    ServiceHealthCheck::class.java -> Services

    ApplicationPolicySet::class.java,
    FirewallPolicy::class.java,
    FirewallRule::class.java,
    AddressGroup::class.java,
    ServiceGroup::class.java -> Security

    else -> null
}