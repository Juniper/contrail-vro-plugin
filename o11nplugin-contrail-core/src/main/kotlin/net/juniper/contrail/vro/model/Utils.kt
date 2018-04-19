/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.PortType
import net.juniper.contrail.api.types.RouteType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.VnSubnetsType
import net.juniper.contrail.api.types.AllowedAddressPair
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.vro.format.PropertyFormatter
import java.util.UUID

class Utils {
    private val minPort = 0
    private val maxPort = 65535

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

    fun formatSubnet(subnet: SubnetType?): String? {
        if (subnet == null) return null
        if (subnet.ipPrefix == null || subnet.ipPrefixLen == null) return null
        return subnet.run { "$ipPrefix/$ipPrefixLen" }
    }

    fun parseSubnet(def: String?): SubnetType? {
        if (def == null || def.isBlank()) return null
        val cleaned = def.trim()
        val parts = cleaned.split(":")
        val (virtualNetworkName, virtualNetworkAddress) = when (parts.size) {
            1 -> Pair(null, parts[0])
            2 -> Pair(parts[0], parts[1])
            else -> throw IllegalArgumentException("Wrong subnet format. use CIDR or VN:CIDR")
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
                network?.name
            } else {
                // "any" or "local"
                networkType
            }
        } else null
        val policyName = if (type == "Policy" && policy != null) policy.name else null
        val securityGroupName = if (type == "Security Group") {
            securityGroup?.qualifiedName?.joinToString(":") ?: "local"
        } else null
        return AddressType(subnet, networkName, securityGroupName, policyName)
    }

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

    fun stringToRuleFromNetworkPolicy(ruleString: String, policy: NetworkPolicy): PolicyRuleType {
        val index = ruleString.split(":")[0].toInt()
        return policy.entries.policyRule[index]
    }

    fun stringToRuleFromSecurityGroup(ruleString: String, group: SecurityGroup): PolicyRuleType {
        val index = ruleString.split(":")[0].toInt()
        return group.entries.policyRule[index]
    }

    fun ruleStringToIndex(ruleString: String): Int =
        ruleString.split(":")[0].toInt()

    fun routeToString(route: RouteType, index: Int): String {
        return "$index: prefix ${route.prefix} next-hop-type ${route.nextHopType} next-hop ${route.nextHop} " +
            "community-attributes ${route.communityAttributes?.communityAttribute?.joinToString(",") ?: ""}"
    }

    fun routeStringToIndex(routeString: String): Int {
        return routeString.split(":")[0].toInt()
    }

    fun ipamSubnetToString(ipamSubnet: IpamSubnetType?): String? =
        ipamSubnet?.run { "${subnet.ipPrefix}/${subnet.ipPrefixLen}" }

    fun removeSubnetFromIpam(cidr: String, ipam : NetworkIpam) {
        val ipPrefix = parseSubnetIP(cidr)
        val ipPrefixLen = parseSubnetPrefix(cidr).toInt()
        ipam.ipamSubnets.subnets.removeIf { it.subnet.ipPrefix == ipPrefix && it.subnet.ipPrefixLen == ipPrefixLen }
    }

    fun lowercase(s: String) =
        s.toLowerCase()

    fun trimList(s: List<String>) : List<String> =
        s.trimList()

    fun isBlankList(s: List<String>?) : Boolean =
        s.isBlankList()
}