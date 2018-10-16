/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.GlobalSystemConfig
import net.juniper.contrail.api.types.GlobalVrouterConfig
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
    private val defaultGlobalSystemConfigFQN = "default-global-system-config"
    private val defaultGlobalVrouterConfigFQN = "$defaultGlobalSystemConfigFQN:default-global-vrouter-config"

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

    fun Connection.listTagsOfType(tagType: String): List<Tag> =
        list<Tag>()?.asSequence()?.filter { isTagOfType(it, tagType) }?.toList() ?: emptyList()

    fun Connection.globalSystemConfig(): GlobalSystemConfig =
        findByFQN(defaultGlobalSystemConfigFQN)!!

    fun Connection.globalVrouterConfig(): GlobalVrouterConfig =
        findByFQN(defaultGlobalVrouterConfigFQN)!!

    private fun Connection.isTagOfType(tag: Tag, tagType: String): Boolean {
        tag.typeName ?: read(tag)
        return tag.typeName == tagType
    }

    fun Connection.commitGlobalDrafts() {
        val globalSystemConfig = findByFQN<GlobalSystemConfig>(defaultGlobalSystemConfigFQN)!!
        commitDrafts(globalSystemConfig)
    }

    fun Connection.discardGlobalDrafts() {
        val globalSystemConfig = findByFQN<GlobalSystemConfig>(defaultGlobalSystemConfigFQN)!!
        discardDrafts(globalSystemConfig)
    }

    fun ServiceGroup.servicePropertyProtocol(ruleString: String): String? =
        findService(ruleString)?.protocol

    fun ServiceGroup.servicePropertyPort(ruleString: String): String? =
        findService(ruleString)?.dstPorts?.let { utils.formatPort(it) }

    // workaround for bug 1797825 - tag_refs field must not exist in the request JSON
    fun FirewallRule.nullifyTag() {
        val tagref = FirewallRule::class.java.getDeclaredField("tag_refs")
        tagref.isAccessible = true
        tagref.set(this, null)
    }

    private fun ServiceGroup.findService(ruleString: String) =
        firewallServiceList?.firewallService?.getOrNull(ruleString.toIndex())
}