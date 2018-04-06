/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.InstanceIp
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork

class Executor(private val connection: Connection) {
    fun VirtualNetwork.subnets(): List<Subnet> {
        val ipams = networkIpam ?: return emptyList()
        return ipams.asSequence().map {
            it.attr.ipamSubnets.asSequence().map { it.subnet() }.filterNotNull()
        }.flatten().toList()
    }

    fun IpamSubnetType.subnet(): Subnet? {
        return connection.findById(subnetUuid)
    }

    // InstanceIp should be in 1-1 relation to VMI, so only first element is chosen if it exists
    fun VirtualMachineInterface.instanceIp(): InstanceIp? =
        instanceIpBackRefs?.getOrNull(0)?.uuid?.let { connection.findById(it) }

    fun ServiceInstance.hasInterfaceWithName(name: String): Boolean? =
        connection.findById<ServiceTemplate>(serviceTemplate[0].uuid)!!.hasInterfaceWithName(name)

    fun ServiceInstance.interfaceNames() : List<String> =
        connection.findById<ServiceTemplate>(serviceTemplate[0].uuid).interfaceNames()

    fun ServiceTemplate.hasInterfaceWithName(name: String?) : Boolean =
        name in interfaceNames()

    fun ServiceTemplate?.interfaceNames() : List<String> =
        this?.properties?.interfaceType?.map { it.serviceInterfaceType } ?: emptyList()

    fun ServiceInstance.networkOfServiceInterface(name: String): VirtualNetwork? {
        val template = connection.findById<ServiceTemplate>(serviceTemplate[0].uuid)!!
        val interfaceNames = template.properties.interfaceType.map { it.serviceInterfaceType }
        val index = interfaceNames.indexOf(name)
        if (properties.interfaceList.size <= index || index < 0) return null
        return connection.findByFQN(properties.interfaceList[index].virtualNetwork)
    }
}