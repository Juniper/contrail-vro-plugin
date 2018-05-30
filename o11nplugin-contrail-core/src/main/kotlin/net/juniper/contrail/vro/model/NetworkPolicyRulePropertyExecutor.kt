/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.MirrorActionType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualNetwork

interface NetworkPolicyRuleProperties {

    fun NetworkPolicy.rulePropertySimpleAction(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyProtocol(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyDirection(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyDstAddressType(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertySrcAddressType(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyDstVirtualNetwork(
        ruleString: String
    ): VirtualNetwork?

    fun NetworkPolicy.rulePropertySrcVirtualNetwork(
        ruleString: String
    ): VirtualNetwork?

    fun NetworkPolicy.rulePropertyDstNetworkPolicy(
        ruleString: String
    ): NetworkPolicy?

    fun NetworkPolicy.rulePropertySrcNetworkPolicy(
        ruleString: String
    ): NetworkPolicy?

    fun NetworkPolicy.rulePropertyDstSecurityGroup(
        ruleString: String
    ): SecurityGroup?

    fun NetworkPolicy.rulePropertySrcSecurityGroup(
        ruleString: String
    ): SecurityGroup?

    fun NetworkPolicy.rulePropertyDstSubnet(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertySrcSubnet(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyDstVirtualNetworkType(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertySrcVirtualNetworkType(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyDstPorts(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertySrcPorts(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyLog(
        ruleString: String
    ): Boolean?

    fun NetworkPolicy.rulePropertyDefineServices(
        ruleString: String
    ): Boolean

    fun NetworkPolicy.rulePropertyServices(
        ruleString: String
    ): List<ServiceInstance?>

    fun NetworkPolicy.rulePropertyDefineMirror(
        ruleString: String
    ): Boolean

    fun NetworkPolicy.rulePropertyMirrorType(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorAnalyzerName(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorAnalyzerInstance(
        ruleString: String
    ): ServiceInstance?

    fun NetworkPolicy.rulePropertyMirrorNicAssistedVlan(
        ruleString: String
    ): Int?

    fun NetworkPolicy.rulePropertyMirrorAnalyzerIP(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorAnalyzerMac(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorUdpPort(
        ruleString: String
    ): Int?

    fun NetworkPolicy.rulePropertyMirrorJuniperHeader(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorRoutingInstance(
        ruleString: String
    ): VirtualNetwork?

    fun NetworkPolicy.rulePropertyMirrorNexthopMode(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorVtepDestIp(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorVtepDestMac(
        ruleString: String
    ): String?

    fun NetworkPolicy.rulePropertyMirrorVni(
        ruleString: String
    ): Int?
}

open class NetworkPolicyRulePropertyExecutor(private val connection: Connection) : NetworkPolicyRuleProperties {
    override fun NetworkPolicy.rulePropertySimpleAction(ruleString: String): String? {
        val rule = extractRule(ruleString)
        return rule.actionList.simpleAction
    }

    override fun NetworkPolicy.rulePropertyProtocol(ruleString: String): String? {
        val rule = extractRule(ruleString)
        return rule.protocol
    }

    override fun NetworkPolicy.rulePropertyDirection(ruleString: String): String? {
        val rule = extractRule(ruleString)
        return rule.direction
    }

    override fun NetworkPolicy.rulePropertyDstAddressType(
        ruleString: String
    ): String? = rulePropertyAddressType(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcAddressType(
        ruleString: String
    ): String? = rulePropertyAddressType(ruleString, false)

    override fun NetworkPolicy.rulePropertyDstVirtualNetwork(
        ruleString: String
    ): VirtualNetwork? = ruleAddressPropertyVirtualNetwork(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcVirtualNetwork(
        ruleString: String
    ): VirtualNetwork? = ruleAddressPropertyVirtualNetwork(ruleString, false)

    override fun NetworkPolicy.rulePropertyDstNetworkPolicy(
        ruleString: String
    ): NetworkPolicy? = ruleAddressPropertyNetworkPolicy(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcNetworkPolicy(
        ruleString: String
    ): NetworkPolicy? = ruleAddressPropertyNetworkPolicy(ruleString, false)

    override fun NetworkPolicy.rulePropertyDstSecurityGroup(
        ruleString: String
    ): SecurityGroup? = ruleAddressPropertySecurityGroup(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcSecurityGroup(
        ruleString: String
    ): SecurityGroup? = ruleAddressPropertySecurityGroup(ruleString, false)

    override fun NetworkPolicy.rulePropertyDstSubnet(
        ruleString: String
    ): String? = ruleAddressPropertySubnet(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcSubnet(
        ruleString: String
    ): String? = ruleAddressPropertySubnet(ruleString, false)

    override fun NetworkPolicy.rulePropertyDstVirtualNetworkType(
        ruleString: String
    ): String? = ruleAddressPropertyNetworkType(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcVirtualNetworkType(
        ruleString: String
    ): String? = ruleAddressPropertyNetworkType(ruleString, false)

    override fun NetworkPolicy.rulePropertyDstPorts(
        ruleString: String
    ): String? = ruleAddressPropertyPorts(ruleString, true)
    override fun NetworkPolicy.rulePropertySrcPorts(
        ruleString: String
    ): String? = ruleAddressPropertyPorts(ruleString, false)

    override fun NetworkPolicy.rulePropertyLog(ruleString: String): Boolean? {
        val rule = extractRule(ruleString)
        return rule.actionList.log
    }

    override fun NetworkPolicy.rulePropertyDefineServices(
        ruleString: String
    ): Boolean {
        val rule = extractRule(ruleString)
        val services = rule.actionList.applyService
        return services != null
    }

    override fun NetworkPolicy.rulePropertyServices(
        ruleString: String
    ): List<ServiceInstance?> {
        val rule = extractRule(ruleString)
        val services = rule.actionList.applyService
        return services.map { connection.findByFQN<ServiceInstance>(it) }
    }

    override fun NetworkPolicy.rulePropertyDefineMirror(
        ruleString: String
    ): Boolean {
        return extractMirror(ruleString) != null
    }

    override fun NetworkPolicy.rulePropertyMirrorType(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return when {
            mirror.nicAssistedMirroring -> "NIC Assisted"
            mirror.analyzerIpAddress != null -> "Analyzer IP"
            else -> "Analyzer Instance"
        }
    }

    override fun NetworkPolicy.rulePropertyMirrorAnalyzerName(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.analyzerName
    }

    override fun NetworkPolicy.rulePropertyMirrorAnalyzerInstance(
        ruleString: String
    ): ServiceInstance? {
        val mirror = extractMirror(ruleString) ?: return null
        return connection.findByFQN(mirror.analyzerName)
    }

    override fun NetworkPolicy.rulePropertyMirrorNicAssistedVlan(
        ruleString: String
    ): Int? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.nicAssistedMirroringVlan
    }

    override fun NetworkPolicy.rulePropertyMirrorAnalyzerIP(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.analyzerIpAddress
    }

    override fun NetworkPolicy.rulePropertyMirrorAnalyzerMac(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.analyzerMacAddress
    }

    override fun NetworkPolicy.rulePropertyMirrorUdpPort(
        ruleString: String
    ): Int? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.udpPort
    }

    override fun NetworkPolicy.rulePropertyMirrorJuniperHeader(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return if (mirror.juniperHeader) "enabled" else "disabled"
    }

    // FQN had the network ID twice at the end
    override fun NetworkPolicy.rulePropertyMirrorRoutingInstance(
        ruleString: String
    ): VirtualNetwork? {
        val mirror = extractMirror(ruleString) ?: return null
        val routingInstanceFQN = fixDoubleIdFqn(mirror.routingInstance)
        return connection.findByFQN(routingInstanceFQN)
    }

    override fun NetworkPolicy.rulePropertyMirrorNexthopMode(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.nhMode
    }

    override fun NetworkPolicy.rulePropertyMirrorVtepDestIp(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.staticNhHeader.vtepDstIpAddress
    }

    override fun NetworkPolicy.rulePropertyMirrorVtepDestMac(
        ruleString: String
    ): String? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.staticNhHeader.vtepDstMacAddress
    }

    override fun NetworkPolicy.rulePropertyMirrorVni(
        ruleString: String
    ): Int? {
        val mirror = extractMirror(ruleString) ?: return null
        return mirror.staticNhHeader.vni
    }

    private fun NetworkPolicy.rulePropertyAddressType(
        ruleString: String,
        useDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, useDstAddressType)
        return utils.addressType(address)
    }

    private fun NetworkPolicy.ruleAddressPropertyVirtualNetwork(
        ruleString: String,
        useDstAddressType: Boolean
    ): VirtualNetwork? {
        val address = extractAddressType(ruleString, useDstAddressType)
        return connection.findByFQN(address.virtualNetwork)
    }

    private fun NetworkPolicy.ruleAddressPropertyNetworkPolicy(
        ruleString: String,
        useDstAddressType: Boolean
    ): NetworkPolicy? {
        val address = extractAddressType(ruleString, useDstAddressType)
        return connection.findByFQN(address.networkPolicy)
    }

    private fun NetworkPolicy.ruleAddressPropertySecurityGroup(
        ruleString: String,
        useDstAddressType: Boolean
    ): SecurityGroup? {
        val address = extractAddressType(ruleString, useDstAddressType)
        return connection.findByFQN(address.securityGroup)
    }

    private fun NetworkPolicy.ruleAddressPropertySubnet(
        ruleString: String,
        useDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, useDstAddressType)
        return utils.subnetToString(address.subnet)
    }

    private fun NetworkPolicy.ruleAddressPropertyNetworkType(
        ruleString: String,
        useDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, useDstAddressType)
        return when (address.virtualNetwork) {
            null -> null
            "any", "local" -> address.virtualNetwork
            else -> "reference"
        }
    }

    private fun NetworkPolicy.ruleAddressPropertyPorts(
        ruleString: String,
        useDstAddressType: Boolean
    ): String? {
        val rule = extractRule(ruleString)
        val ports = if (useDstAddressType) rule.dstPorts else rule.srcPorts
        return utils.formatPorts(ports)
    }

    private fun NetworkPolicy.extractAddressType(
        ruleString: String,
        useDstAddressType: Boolean
    ): AddressType {
        val rule = extractRule(ruleString)
        val addresses = if (useDstAddressType) rule.dstAddresses else rule.srcAddresses
        // rules only use 0-th element of the address list
        return addresses[0]
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
        val ruleIndex = utils.stringToIndex(ruleString)
        return entries.policyRule[ruleIndex]
    }

    private fun fixDoubleIdFqn(
        doubleIdFqn: String
    ): String =
        doubleIdFqn.split(":").dropLast(1).joinToString(":")
}