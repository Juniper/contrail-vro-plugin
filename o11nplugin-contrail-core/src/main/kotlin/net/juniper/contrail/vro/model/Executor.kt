/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.InstanceIp
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.ServiceInstance

class Executor(private val connection: Connection) {
    private val utils = Utils()

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

    fun SecurityGroup.ruleAddressProperty(
        ruleString: String,
        getDstAddressType: Boolean,
        propertyName: String
    ): Any? {
        val ruleIndex = Utils().ruleStringToIndex(ruleString)
        val rule = entries.policyRule[ruleIndex]
        return ruleAddressProperty(
            rule,
            getDstAddressType,
            propertyName
        )
    }

    private fun ruleAddressProperty(
        rule: PolicyRuleType,
        getDstAddressType: Boolean,
        propertyName: String
    ): Any? {
        val address = if (getDstAddressType) {
            rule.dstAddresses[0]
        } else {
            rule.srcAddresses[0]
        }
        return when (propertyName) {
            "virtualNetwork" ->
                connection.findByFQN<VirtualNetwork>(address.virtualNetwork)
            "securityGroup" ->
                connection.findByFQN<SecurityGroup>(address.securityGroup)
            "networkPolicy" ->
                connection.findByFQN<NetworkPolicy>(address.networkPolicy)
            "subnet" ->
                "${address.subnet.ipPrefix}/${address.subnet.ipPrefixLen}"
            else -> null
        }
    }

    fun NetworkPolicy.ruleAddressPropertyNetwork(
        ruleString: String,
        getDstAddressType: Boolean
    ): VirtualNetwork? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return connection.findByFQN(address.virtualNetwork)
    }

    fun NetworkPolicy.ruleAddressPropertyPolicy(
        ruleString: String,
        getDstAddressType: Boolean
    ): NetworkPolicy? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return connection.findByFQN(address.networkPolicy)
    }

    fun NetworkPolicy.ruleAddressPropertySG(
        ruleString: String,
        getDstAddressType: Boolean
    ): SecurityGroup? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return connection.findByFQN(address.securityGroup)
    }

    fun NetworkPolicy.ruleAddressPropertySubnet(
        ruleString: String,
        getDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return utils.formatSubnet(address.subnet)
    }

    fun NetworkPolicy.ruleAddressPropertyNetworkType(
        ruleString: String,
        getDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return when (address.virtualNetwork) {
            null -> null
            "any", "local" -> address.virtualNetwork
            else -> "reference"
        }
    }

    fun NetworkPolicy.ruleAddressPropertyPorts(
        ruleString: String,
        getDstAddressType: Boolean
    ): String? {
        val rule = extractRule(ruleString)
        val ports = if (getDstAddressType) {
            rule.dstPorts
        } else {
            rule.srcPorts
        }
        return utils.formatPorts(ports)
    }

    fun NetworkPolicy.rulePropertyDefineService(
        ruleString: String
    ): Boolean {
        val rule = extractRule(ruleString)
        val services = rule.actionList.applyService
        return services != null
    }

    fun NetworkPolicy.rulePropertyServices(
        ruleString: String
    ): List<ServiceInstance?> {
        val rule = extractRule(ruleString)
        val services = rule.actionList.applyService
        return services.map { connection.findByFQN<ServiceInstance>(it) }
    }

    fun NetworkPolicy.rulePropertyDefineMirror(
        ruleString: String
    ): Boolean {
        val rule = extractRule(ruleString)
        return rule.actionList.mirrorTo != null
    }

    private fun NetworkPolicy.extractAddressType(
        ruleString: String,
        getDstAddressType: Boolean
    ): AddressType {
        val rule = extractRule(ruleString)
        return if (getDstAddressType) {
            rule.dstAddresses[0]
        } else {
            rule.srcAddresses[0]
        }
    }

    private fun NetworkPolicy.extractRule(
        ruleString: String
    ): PolicyRuleType {
        val ruleIndex = utils.ruleStringToIndex(ruleString)
        return entries.policyRule[ruleIndex]
    }
}