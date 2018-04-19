/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.InstanceIp
import net.juniper.contrail.api.types.MirrorActionType
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

    fun NetworkPolicy.rulePropertyDefineServices(
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
        return extractMirror(ruleString) != null
    }

    fun NetworkPolicy.rulePropertyMirrorType(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        if (mirror.nicAssistedMirroring) {
            return "NIC Assisted"
        }
        if (mirror.analyzerIpAddress != null) {
            return "Analyzer IP"
        }
        return "Analyzer Instance"
    }

    fun NetworkPolicy.rulePropertyMirrorAnalyzerName(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.analyzerName
    }

    fun NetworkPolicy.rulePropertyMirrorAnalyzerInstance(
        ruleString: String
    ): ServiceInstance? {
        val mirror = extractMirror(ruleString) ?: return null
        return connection.findByFQN(mirror.analyzerName)
    }

    fun NetworkPolicy.rulePropertyMirrorNicAssistedVlan(
        ruleString: String
    ): Int? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.nicAssistedMirroringVlan
    }

    fun NetworkPolicy.rulePropertyMirrorAnalyzerIP(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.analyzerIpAddress
    }

    fun NetworkPolicy.rulePropertyMirrorAnalyzerMac(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.analyzerMacAddress
    }

    fun NetworkPolicy.rulePropertyMirrorUdpPort(
        ruleString: String
    ): Int? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.udpPort
    }

    fun NetworkPolicy.rulePropertyMirrorJuniperHeader(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return if (mirror.juniperHeader) {
            "enabled"
        } else {
            "disabled"
        }
    }

    // FQN had the network ID twice at the end
    fun NetworkPolicy.rulePropertyMirrorRoutingInstance(
        ruleString: String
    ): VirtualNetwork? {
        val mirror = extractMirror(ruleString) ?: return null
        val routingInstanceFQN = fixDoubleIdFqn(mirror.routingInstance)
        return connection.findByFQN(routingInstanceFQN)
    }

    fun NetworkPolicy.rulePropertyMirrorNexthopMode(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.nhMode
    }

    fun NetworkPolicy.rulePropertyMirrorVtepDestIp(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.staticNhHeader.vtepDstIpAddress
    }

    fun NetworkPolicy.rulePropertyMirrorVtepDestMac(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.staticNhHeader.vtepDstMacAddress
    }

    fun NetworkPolicy.rulePropertyMirrorVni(
        ruleString: String
    ): Int? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.staticNhHeader.vni
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

    private fun NetworkPolicy.extractMirror(
        ruleString: String
    ): MirrorActionType? {
        val rule = extractRule(ruleString)
        return rule.actionList.mirrorTo
    }

    private fun NetworkPolicy.extractRule(
        ruleString: String
    ): PolicyRuleType {
        val ruleIndex = utils.ruleStringToIndex(ruleString)
        return entries.policyRule[ruleIndex]
    }

    fun SecurityGroup.rulePropertyDirection(
        ruleString: String
    ): String? {
        return determineDirection(ruleString)
    }

    fun SecurityGroup.rulePropertyAddressType(
        ruleString: String
    ): String? {
        val address = extractAddress(ruleString)
        return addressType(address)
    }

    fun SecurityGroup.rulePropertyAddressCidr(
        ruleString: String
    ): String? {
        val address = extractAddress(ruleString)
        return utils.formatSubnet(address.subnet)
    }

    fun SecurityGroup.rulePropertyAddressSecurityGroup(
        ruleString: String
    ): SecurityGroup? {
        val address = extractAddress(ruleString)
        return connection.findByFQN(address.securityGroup)
    }

    fun SecurityGroup.rulePropertyPorts(
        ruleString: String
    ): String? {
        val direction = determineDirection(ruleString)
        val rule = extractRule(ruleString)
        val ports = if (direction == "ingress") {
            rule.srcPorts
        } else {
            rule.dstPorts
        }
        return utils.formatPorts(ports)
    }

    private fun SecurityGroup.extractAddress(
        ruleString: String
    ): AddressType {
        val direction = determineDirection(ruleString)
        val rule = extractRule(ruleString)
        return if (direction == "ingress") {
            rule.srcAddresses[0]
        } else {
            rule.dstAddresses[0]
        }
    }

    private fun SecurityGroup.determineDirection(
        ruleString: String
    ): String? {
        val rule = extractRule(ruleString)
        return if (rule.srcAddresses[0].securityGroup == "local") {
            "egress"
        } else {
            "ingress"
        }
    }

    private fun SecurityGroup.extractRule(
        ruleString: String
    ): PolicyRuleType {
        val ruleIndex = utils.ruleStringToIndex(ruleString)
        return entries.policyRule[ruleIndex]
    }

    private fun fixDoubleIdFqn(
        doubleIdFqn: String
    ): String =
        doubleIdFqn.split(":").dropLast(1).joinToString(":")

    // TODO Merge somehow with the same function in Utils.kt
    private fun addressType(
        address: AddressType
    ): String? {
        if (address.subnet != null) {
            return "CIDR"
        }
        if (address.networkPolicy != null) {
            return "Policy"
        }
        if (address.virtualNetwork != null) {
            return "Network"
        }
        if (address.securityGroup != null) {
            return "Security Group"
        }
        if (address.subnetList != null) {
            // Currently unused
            return "CIDR"
        }
        return null
    }
}