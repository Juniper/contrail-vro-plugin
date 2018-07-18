/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.InstanceIp
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.TagType

// To reduce complexity of one class, the functionality has been split between multiple simpler classes.
// They are combined using inheritance so that their functions become Executor's methods.
class Executor(private val connection: Connection) :
SecurityGroupRuleProperties by SecurityGroupRulePropertyExecutor(connection),
NetworkPolicyRuleProperties by NetworkPolicyRulePropertyExecutor(connection),
FirewallRuleComplexProperties by FirewallRuleComplexPropertyExecutor(connection) {
    fun VirtualNetwork.subnets(): List<IpamSubnetType> {
        val ipams = networkIpam ?: return emptyList()
        return ipams.asSequence().map { it.attr.ipamSubnets.asSequence().filterNotNull() }.flatten().toList()
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

    fun Connection.listTagTypes(): List<String> =
        list<TagType>()?.asSequence()?.map { it.name }?.sorted()?.toList() ?: emptyList()

    fun Connection.listLabels(): List<Tag> =
        list<Tag>()?.asSequence()?.filter { isLabel(it) }?.toList() ?: emptyList()

    private fun Connection.isLabel(tag: Tag): Boolean {
        tag.typeName ?: read(tag)
        return tag.typeName == "label"
    }

    fun ServiceGroup.servicePropertyProtocol(ruleString: String): String? =
        findService(ruleString)?.protocol

    fun ServiceGroup.servicePropertyPort(ruleString: String): String? =
        findService(ruleString)?.dstPorts?.let { utils.formatPort(it) }

    private fun ServiceGroup.findService(ruleString: String) =
        firewallServiceList?.firewallService?.getOrNull(ruleString.toIndex())
}