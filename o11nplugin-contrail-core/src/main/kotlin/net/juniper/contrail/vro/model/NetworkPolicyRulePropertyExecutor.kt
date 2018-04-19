package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.MirrorActionType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualNetwork

interface NetworkPolicyRuleProperties {

    fun NetworkPolicy.rulePropertyAddressType(
        ruleString: String,
        getDstAddressType: Boolean
    ): String?

    fun NetworkPolicy.ruleAddressPropertyNetwork(
        ruleString: String,
        getDstAddressType: Boolean
    ): VirtualNetwork?

    fun NetworkPolicy.ruleAddressPropertyPolicy(
        ruleString: String,
        getDstAddressType: Boolean
    ): NetworkPolicy?

    fun NetworkPolicy.ruleAddressPropertySG(
        ruleString: String,
        getDstAddressType: Boolean
    ): SecurityGroup?

    fun NetworkPolicy.ruleAddressPropertySubnet(
        ruleString: String,
        getDstAddressType: Boolean
    ): String?

    fun NetworkPolicy.ruleAddressPropertyNetworkType(
        ruleString: String,
        getDstAddressType: Boolean
    ): String?

    fun NetworkPolicy.ruleAddressPropertyPorts(
        ruleString: String,
        getDstAddressType: Boolean
    ): String?

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
    private val utils = Utils()

    override fun NetworkPolicy.rulePropertyAddressType(
        ruleString: String,
        getDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return utils.addressType(address)
    }

    override fun NetworkPolicy.ruleAddressPropertyNetwork(
        ruleString: String,
        getDstAddressType: Boolean
    ): VirtualNetwork? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return connection.findByFQN(address.virtualNetwork)
    }

    override fun NetworkPolicy.ruleAddressPropertyPolicy(
        ruleString: String,
        getDstAddressType: Boolean
    ): NetworkPolicy? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return connection.findByFQN(address.networkPolicy)
    }

    override fun NetworkPolicy.ruleAddressPropertySG(
        ruleString: String,
        getDstAddressType: Boolean
    ): SecurityGroup? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return connection.findByFQN(address.securityGroup)
    }

    override fun NetworkPolicy.ruleAddressPropertySubnet(
        ruleString: String,
        getDstAddressType: Boolean
    ): String? {
        val address = extractAddressType(ruleString, getDstAddressType)
        return utils.formatSubnet(address.subnet)
    }

    override fun NetworkPolicy.ruleAddressPropertyNetworkType(
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

    override fun NetworkPolicy.ruleAddressPropertyPorts(
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
        if (mirror.nicAssistedMirroring) {
            return "NIC Assisted"
        }
        if (mirror.analyzerIpAddress != null) {
            return "Analyzer IP"
        }
        return "Analyzer Instance"
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
        return if (mirror.juniperHeader) {
            "enabled"
        } else {
            "disabled"
        }
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

    private fun fixDoubleIdFqn(
        doubleIdFqn: String
    ): String =
        doubleIdFqn.split(":").dropLast(1).joinToString(":")
}