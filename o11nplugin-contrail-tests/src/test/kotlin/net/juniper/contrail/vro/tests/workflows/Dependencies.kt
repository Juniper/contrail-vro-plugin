/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.gen.FloatingIpPool_Wrapper
import net.juniper.contrail.vro.gen.FloatingIp_Wrapper
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.gen.Project_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper
import net.juniper.contrail.vro.gen.ServiceInstance_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterface_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import java.util.UUID

fun randomStringUuid() = UUID.randomUUID().toString()

class Dependencies(private val connection: Connection_Wrapper) {
    fun someProject(): Project_Wrapper = Project_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProject$uuid"
        setParentConnection(this@Dependencies.connection)
    }

    @JvmOverloads
    fun someVirtualNetwork(parent: Project_Wrapper = someProject()) = VirtualNetwork_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someVirtualNetwork$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someFloatingIpPool(parent: VirtualNetwork_Wrapper = someVirtualNetwork()) = FloatingIpPool_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someFloatingIpPool$uuid"
        setParentVirtualNetwork(parent)
    }

    @JvmOverloads
    fun someNetworkIpam(parent: Project_Wrapper = someProject()) = NetworkIpam_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someNetworkIpam$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun somePort(parent: Project_Wrapper = someProject()) = VirtualMachineInterface_Wrapper().apply {
        uuid = randomStringUuid()
        name = "somePort$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someFloatingIp(parent: FloatingIpPool_Wrapper = someFloatingIpPool()) = FloatingIp_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someFloatingIp$uuid"
        setParentFloatingIpPool(parent)
    }

    @JvmOverloads
    fun someNetworkPolicy(parent: Project_Wrapper = someProject()) = NetworkPolicy_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someNetworkPolicy$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someServiceInstance(parent: Project_Wrapper = someProject()) = ServiceInstance_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someServiceInstance$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someSecurityGroup(parent: Project_Wrapper = someProject()) = SecurityGroup_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someSecurityGroup$uuid"
        setParentProject(parent)
    }

    fun somePortProperties() = VirtualMachineInterfacePropertiesType_Wrapper()
}
