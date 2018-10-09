/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.AllowedAddressPair
import net.juniper.contrail.api.types.FirewallRuleEndpointType
import net.juniper.contrail.api.types.FirewallServiceType
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.PortType
import net.juniper.contrail.api.types.RouteType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.VnSubnetsType
import net.juniper.contrail.vro.base.Description
import net.juniper.contrail.vro.config.constants.EndpointType
import net.juniper.contrail.vro.config.constants.maxPort
import net.juniper.contrail.vro.config.constants.minPort
import net.juniper.contrail.vro.format.PropertyFormatter
import java.util.UUID

@Description("Object containing miscellaneous utility functions used by workflows and actions, e.g. IP address validation.")
class Utils {
    private val macPattern =
        "^(?:[\\p{XDigit}]{1,2}([-:]))(?:[\\p{XDigit}]{1,2}\\1){4}[\\p{XDigit}]{1,2}$".toRegex()

    private val integerPattern = "\\d+".toRegex()

    private val whitespacePattern = "\\s+".toRegex()

    fun isValidAddress(input: String): Boolean =
        NetAddressValidator.isValidAddress(input)

    fun isValidIpv4Address(input: String): Boolean =
        NetAddressValidator.IPv4.isValidAddress(input)

    fun isValidIpv6Address(input: String): Boolean =
        NetAddressValidator.IPv6.isValidAddress(input)

    fun isValidSubnet(input: String): Boolean =
        NetAddressValidator.isValidSubnet(input)

    fun isValidIpv6Subnet(input: String): Boolean =
        NetAddressValidator.IPv6.isValidSubnet(input)

    fun isValidIpv4Subnet(input: String): Boolean =
        NetAddressValidator.IPv4.isValidSubnet(input)

    fun isValidCidr(input: String): Boolean =
        NetAddressValidator.isValidCidr(input)

    fun isValidIpv6Cidr(input: String): Boolean =
        NetAddressValidator.IPv6.isValidCidr(input)

    fun isValidIpv4Cidr(input: String): Boolean =
        NetAddressValidator.IPv4.isValidCidr(input)

    fun isValidPool(input: List<String>): Boolean =
        NetAddressValidator.areValidPools(input)

    fun isValidIpv4Pool(input: List<String>): Boolean =
        NetAddressValidator.IPv4.areValidPools(input)

    fun isValidIpv6Pool(input: List<String>): Boolean =
        NetAddressValidator.IPv6.areValidPools(input)

    fun isValidAllocationPool(cidr: String, pools: List<String>): Boolean {
        if (isValidIpv4Subnet(cidr) && isValidIpv4Pool(pools)) {
            return parsePools(cidr, pools, ::IPv4)
        } else if (isValidIpv6Subnet(cidr) && isValidIpv6Pool(pools)) {
            return parsePools(cidr, pools, ::IPv6)
        }
        return false
    }

    fun isInCidr(cidr: String, address: String): Boolean {
        if (isValidIpv4Address(address) && isValidIpv4Subnet(cidr)) {
            return IPv4(address) in subnetRange(cidr, ::IPv4)
        } else if (isValidIpv6Address(address) && isValidIpv6Subnet(cidr)) {
            return IPv6(address) in subnetRange(cidr, ::IPv6)
        }
        return false
    }

    fun isFree(cidr: String, address: String, pools: List<String>?, dnsAddr: String?): Boolean {
        if (isValidIpv4Subnet(cidr) && isValidIpv4Address(address)) {
            if (pools != null && !pools.isBlankList() && !isValidIpv4Pool(pools)) return false
            if (dnsAddr != null && dnsAddr.isNotBlank() && !isValidIpv4Address(dnsAddr)) return false
            val ip = IPv4(address)
            return (ip in subnetRange(cidr, ::IPv4)) && ip.notInPools(pools, ::IPv4)
                    && !dnsAddr.equalsIp(ip, ::IPv4)
        } else if (isValidIpv6Subnet(cidr) && isValidIpv6Address(address)) {
            if (pools != null && !pools.isBlankList() && !isValidIpv6Pool(pools)) return false
            if (dnsAddr != null && dnsAddr.isNotBlank() && !isValidIpv6Address(dnsAddr)) return false
            val ip = IPv6(address)
            return (ip in subnetRange(cidr, ::IPv6)) && ip.notInPools(pools, ::IPv6)
                    && !dnsAddr.equalsIp(ip, ::IPv6)
        }
        return false
    }

    fun isValidMacAddress(mac : String) : Boolean =
        mac.matches(macPattern)

    // first part must be an integer in range [0, 65535]; second number must be a positive integer.
    fun isValidCommunityAttribute(communityAttribute: String): Boolean {
        val parts = communityAttribute.split(":")
        if (parts.size != 2) return false
        val firstPartNumericValue = parts[0].toIntOrNull() ?: return false
        if (firstPartNumericValue < minPort || firstPartNumericValue > maxPort) return false
        return parts[1].matches(integerPattern)
    }

    fun getVnSubnet(network: VirtualNetwork, ipam: NetworkIpam): VnSubnetsType =
        network.networkIpam?.find { it.uuid == ipam.uuid }?.attr ?: VnSubnetsType()

    fun isNetworRelatedToIpam(network: VirtualNetwork, ipam: NetworkIpam): Boolean =
        network.networkIpam?.any { it.uuid == ipam.uuid } ?: false

    fun removeSubnetFromVirtualNetwork(network: VirtualNetwork, subnet: String) {
        val ipams = network.networkIpam ?: return
        val ipPrefix = parseSubnetIP(subnet)
        val ipPrefixLen = parseSubnetPrefix(subnet).toInt()

        //first remove subnet from attributes
        ipams.forEach {
            it.attr.ipamSubnets.removeIf { it.subnet.ipPrefix == ipPrefix && it.subnet.ipPrefixLen == ipPrefixLen }
        }
        //then remove IPAMs if have not subnets
        ipams.removeIf {
            it.attr.ipamSubnets.isEmpty()
        }
    }

    private fun String.clean() =
        replace(whitespacePattern, "")

    fun parsePortsOfNetworkPolicyRule(ports: String): List<PortType> {
        val ranges = ports.split(",").map { it.clean() }
        return ranges.map { parsePortRange(it, anyAsFullRange = false) }
    }

    fun parsePortsOfSecurityGroupRule(ports: String): List<PortType> {
        val ranges = ports.split(",").map { it.clean() }
        if (ranges.size > 1) throw IllegalArgumentException("Only one port range is allowed in security group rule")
        return ranges.map { parsePortRange(it, anyAsFullRange = true) }
    }

    fun parsePortsOfFirewallRule(ports: String): PortType =
        parsePortRange(ports, true)

    fun formatPort(port: PortType): String =
        if (port.startPort == port.endPort)
            if (port.startPort == -1) "any" else "${port.startPort}"
        else if (port.startPort == minPort && port.endPort == maxPort)
            "any"
        else
            "${port.startPort}-${port.endPort}"

    fun formatPorts(ports: List<PortType>): String =
        ports.joinToString(",") { formatPort(it) }

    private fun parsePortRange(def: String, anyAsFullRange: Boolean): PortType {
        val ends = def.split("-")
        return when (ends.size) {
            1 ->
                if (ends[0] == "any") {
                    if (anyAsFullRange) {
                        PortType(minPort, maxPort)
                    } else {
                        PortType(-1, -1)
                    }
                } else {
                    val portNumber = parsePort(ends[0])
                    PortType(portNumber, portNumber)
                }
            2 -> {
                val portRangeStart = parsePort(ends[0])
                val portRangeEnd = parsePort(ends[1])

                PortType(portRangeStart, portRangeEnd)
                }
            else -> throw IllegalArgumentException("Invalid port ranges format")
        }
    }

    private fun parsePort(port: String): Int {
        val portNumber = port.toInt()
        if (portNumber < minPort || portNumber > maxPort) {
            throw IllegalArgumentException("Port number $portNumber out of bounds ($minPort-$maxPort)")
        }
        return portNumber
    }

    fun parseSubnet(def: String?): SubnetType? {
        if (def == null || def.isBlank()) return null
        val cleaned = def.trim()
        val parts = cleaned.split(":")
        val (virtualNetworkName, virtualNetworkAddress) = when (parts.size) {
            1 -> Pair(null, parts[0])
            2 -> Pair(parts[0], parts[1])
            4 -> {
                // Domain:Project:NetworkName:CIDR
                val networkFQN = parts.asSequence().take(3).joinToString(":")
                Pair(networkFQN, parts[3])
            }
            else -> throw IllegalArgumentException("Wrong subnet format. use CIDR, VN:CIDR or VN-FQNAME:CIDR")
        }
        if (!isValidCidr(virtualNetworkAddress)) throw IllegalArgumentException("Wrong CIDR format.")
        val (subnetIP, subnetPrefix) = virtualNetworkAddress.split('/')
        val fullNetworkName = if (virtualNetworkName != null) {
            "$virtualNetworkName:$subnetIP"
        } else {
            subnetIP
        }
        return SubnetType(fullNetworkName, subnetPrefix.toInt())
    }

    fun parseSubnetIP(address: String) : String =
        address.trim().split('/')[0]

    fun parseSubnetPrefix(address: String) : String =
        address.trim().split('/')[1]

    private val ApiObjectBase.FQN get() =
        qualifiedName?.joinToString(":")

    fun createAddress(
        type: String,
        cidr: String?,
        networkType: String?,
        network: VirtualNetwork?,
        policy: NetworkPolicy?,
        securityGroup: SecurityGroup?
    ): AddressType {
        val subnet = if (type == "CIDR" && cidr != null) parseSubnet(cidr) else null
        val networkName = if (type == "Network") {
            if (networkType == "reference") {
                network?.FQN
            } else {
                // "any" or "local"
                networkType
            }
        } else null
        val policyName = if (type == "Policy" && policy != null) policy.FQN else null
        val securityGroupName = if (type == "Security Group") {
            securityGroup?.FQN ?: "local"
        } else null
        return AddressType(subnet, networkName, securityGroupName, policyName)
    }

    fun createEndpoint(
        type: String,
        tags: List<Tag>?,
        virtualNetwork: VirtualNetwork?,
        addressGroup: AddressGroup?
    ): FirewallRuleEndpointType {
        val anyWorkload = type == EndpointType.AnyWorkload.value
        val tagNames = if (type == EndpointType.Tag.value && tags != null) tags.map { tagToString(it) } else listOf()
        val virtualNetworkFqn = if (type == EndpointType.VirtualNetwork.value) virtualNetwork?.FQN else null
        val addressGroupFqn = if (type == EndpointType.AddressGroup.value) addressGroup?.FQN else null
        return FirewallRuleEndpointType(null, virtualNetworkFqn, addressGroupFqn, tagNames, null, anyWorkload)
    }

    fun tagToString(tag: Tag): String =
        if (tag.parentType == "project") tag.name else "global:${tag.name}"

    fun randomUUID(): String =
        UUID.randomUUID().toString()

    fun allowedAddressPairToString(pair: AllowedAddressPair, index: Int) : String {
        val mac = if (pair.mac != null && pair.mac.isNotBlank()) "mac ${pair.mac}" else ""
        val addressMode = if (pair.addressMode != null && pair.addressMode.isNotBlank())
            "allowed address mode ${pair.addressMode}" else ""
        val ipPrefix = if (pair.ip != null) "ip ${pair.ip.ipPrefix}" else ""
        val ipPrefixLen = if (pair.ip != null && pair.ip.ipPrefixLen != null) "/${pair.ip.ipPrefixLen}" else ""
        return "$index: $ipPrefix$ipPrefixLen $mac $addressMode"
    }

    fun ruleToString(rule: PolicyRuleType, index: Int): String {
        return "$index: ${rule.actionList?.simpleAction ?: ""} protocol ${rule.protocol} ${PropertyFormatter.format(rule.srcAddresses[0])} ports ${rule.srcPorts.joinToString(", "){PropertyFormatter.format(it)}} " +
            "${rule.direction} ${PropertyFormatter.format(rule.dstAddresses[0])} ports ${rule.dstPorts.joinToString(", "){PropertyFormatter.format(it)}}"
    }

    fun stringToRuleFromNetworkPolicy(ruleString: String, policy: NetworkPolicy): PolicyRuleType =
        policy.entries.policyRule[ruleString.toIndex()]

    fun stringToRuleFromSecurityGroup(ruleString: String, group: SecurityGroup): PolicyRuleType =
        group.entries.policyRule[ruleString.toIndex()]

    fun stringToIndex(ruleString: String): Int =
        ruleString.toIndex()

    fun firewallServiceToString(service: FirewallServiceType, index: Int) =
        "$index: ${PropertyFormatter.format(service)}"

    fun stringToFirewallService(serviceString: String, serviceGroup: ServiceGroup): FirewallServiceType =
        serviceGroup.firewallServiceList.firewallService[serviceString.toIndex()]

    fun parseFirewallServicePorts(ports: String): PortType =
        parsePortRange(ports, anyAsFullRange = true)

    fun routeToString(route: RouteType, index: Int): String {
        return "$index: prefix ${route.prefix} next-hop-type ${route.nextHopType} next-hop ${route.nextHop} " +
            "community-attributes ${route.communityAttributes?.communityAttribute?.joinToString(",") ?: ""}"
    }

    fun routeStringToIndex(routeString: String): Int =
        routeString.split(":")[0].toInt()

    fun subnetToString(subnet: SubnetType?): String? {
        if (subnet == null) return null
        if (subnet.ipPrefix == null || subnet.ipPrefixLen == null) return null
        return subnet.run { "$ipPrefix/$ipPrefixLen" }
    }

    fun ipamSubnetToString(ipamSubnet: IpamSubnetType?): String? =
        subnetToString(ipamSubnet?.subnet)

    fun removeSubnetFromIpam(cidr: String, ipam : NetworkIpam) {
        val ipPrefix = parseSubnetIP(cidr)
        val ipPrefixLen = parseSubnetPrefix(cidr).toInt()
        ipam.ipamSubnets.subnets.removeIf { it.subnet.ipPrefix == ipPrefix && it.subnet.ipPrefixLen == ipPrefixLen }
    }

    fun removeSubnetFromAddressGroup(cidr: String, addressGroup : AddressGroup) {
        val ipPrefix = parseSubnetIP(cidr)
        val ipPrefixLen = parseSubnetPrefix(cidr).toInt()
        addressGroup.prefix.subnet.removeIf { it.ipPrefix == ipPrefix && it.ipPrefixLen == ipPrefixLen }
    }

    fun lowercase(s: String) =
        s.toLowerCase()

    fun trimList(s: List<String>) : List<String> =
        s.trimList()

    fun isBlankList(s: List<String>?) : Boolean =
        s.isBlankList()

    fun addressType(
        address: AddressType
    ): String? = when {
        address.subnet != null ||
        address.subnetList != null -> "CIDR"
        address.networkPolicy != null -> "Policy"
        address.virtualNetwork != null -> "Network"
        address.securityGroup != null -> "Security Group"
        else -> null
    }
}

fun String.toIndex() =
    split(":")[0].toInt()

// Utils is not an object due to model-driven archetype constraints
val utils = Utils()