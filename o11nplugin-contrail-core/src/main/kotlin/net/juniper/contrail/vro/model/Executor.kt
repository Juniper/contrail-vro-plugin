/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.AllowedAddressPair
import net.juniper.contrail.api.types.InstanceIp
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork

class Executor(private val connection: Connection) {

    val id: Sid get() =
        connection.info.sid

    fun subnetsOfVirtualNetwork(parent: VirtualNetwork): List<Subnet> {
        val ipams = parent.networkIpam ?: return emptyList()
        return ipams.asSequence().map {
            it.attr.ipamSubnets.asSequence().map { connection.findById<Subnet>(it.subnetUuid) }.filterNotNull()
        }.flatten().toList()
    }

    // InstanceIp should be in 1-1 relation to VMI, so only first element is chosen if it exists
    fun instanceIpOfPort(port: VirtualMachineInterface): InstanceIp? =
        port.instanceIpBackRefs?.getOrNull(0)?.uuid?.let { connection.findById(it) }

    fun serviceHasInterfaceWithName(serviceInstance: ServiceInstance, name: String): Boolean? {
        val template = connection.findById<ServiceTemplate>(serviceInstance.serviceTemplate[0].uuid)!!
        return templateHasInterfaceWithName(template, name)
    }

    fun interfaceIndexByName(serviceInstance: ServiceInstance, name: String): Int {
        val template = connection.findById<ServiceTemplate>(serviceInstance.serviceTemplate[0].uuid)
        return interfaceNamesFromTemplate(template).indexOf(name)
    }

    fun interfaceNamesFromService(serviceInstance: ServiceInstance) : List<String> =
        interfaceNamesFromTemplate(connection.findById<ServiceTemplate>(serviceInstance.serviceTemplate[0].uuid))

    fun templateHasInterfaceWithName(template: ServiceTemplate, name: String?) : Boolean =
        name in interfaceNamesFromTemplate(template)

    fun interfaceNamesFromTemplate(template: ServiceTemplate?) : List<String> =
        template?.properties?.interfaceType?.map { it.serviceInterfaceType } ?: emptyList()

    fun allowedAddressPairs(instance: ServiceInstance, name: String) : List<AllowedAddressPair> {
        val idx = interfaceNamesFromService(instance).indexOf(name)
        val interfaceList = instance.properties?.interfaceList ?: emptyList()
        if (interfaceList.size <= idx || idx < 0) return emptyList()
        return interfaceList[idx]?.allowedAddressPairs?.allowedAddressPair ?: emptyList()
    }

    fun networkOfServiceInterface(serviceInstance: ServiceInstance, name: String): VirtualNetwork? {
        val template = connection.findById<ServiceTemplate>(serviceInstance.serviceTemplate[0].uuid)!!
        val interfaceNames = template.properties.interfaceType.map { it.serviceInterfaceType }
        val index = interfaceNames.indexOf(name)
        if (serviceInstance.properties.interfaceList.size <= index || index < 0) return null
        return connection.findByFQN(serviceInstance.properties.interfaceList[index].virtualNetwork)
    }
}