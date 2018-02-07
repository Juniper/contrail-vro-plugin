/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.PortType
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.VnSubnetsType
import net.juniper.contrail.vro.format.PropertyFormatter
import java.util.UUID

class Utils {

    fun isValidAddress(input: String): Boolean =
        NetAddressValidator.isValidAddress(input)

    fun isValidIpv4Address(input: String): Boolean =
        NetAddressValidator.IPv4.isValidAddress(input)

    fun isValidIpv6Address(input: String): Boolean =
        NetAddressValidator.IPv6.isValidAddress(input)

    fun isValidCidr(input: String): Boolean =
        NetAddressValidator.isValidSubnet(input)

    fun isValidIpv6Cidr(input: String): Boolean =
        NetAddressValidator.IPv6.isValidSubnet(input)

    fun isValidIpv4Cidr(input: String): Boolean =
        NetAddressValidator.IPv4.isValidSubnet(input)

    fun isValidPool(input: String): Boolean =
        NetAddressValidator.IPv4.areValidPools(input)

    fun isValidAllocationPool(cidr: String, pools: String): Boolean {
        if (isValidIpv4Cidr(cidr) && NetAddressValidator.IPv4.areValidPools(pools)) {
            return parseIpv4Pools(cidr, pools)
        } else if (isValidIpv6Cidr(cidr) && NetAddressValidator.IPv6.areValidPools(pools)) {
            //TODO Include IPv6 support
            return false
        }
        return false
    }

    //TODO Include IPv6 support
    fun isInCidr(cidr: String, address: String): Boolean =
        address.ipToInt().ip() in getSubnetRange(cidr)

    fun isFree(cidr: String, address: String, pools: String?, dnsAddr: String?): Boolean {
        val ip = address.ipToInt().ip()
        return (ip in getSubnetRange(cidr)) && pools.ipNotInPools(ip) && !dnsAddr.equalsIp(ip)
    }

    fun getVnSubnet(network: VirtualNetwork, ipam: NetworkIpam): VnSubnetsType =
        network.networkIpam?.find { it.uuid == ipam.uuid }?.attr ?: VnSubnetsType()

    fun isNetworRelatedToIpam(network: VirtualNetwork, ipam: NetworkIpam): Boolean =
        network.networkIpam?.any { it.uuid == ipam.uuid } ?: false

    private fun String.clean() =
        replace("\\s+", "")

    fun parsePorts(ports: String): List<PortType> {
        val cleaned = ports.clean()
        val ranges = cleaned.split(",")
        return ranges.map { parsePortRange(it) }
    }

    private fun parsePortRange(def: String): PortType {
        val ends = def.split("-")
        return when (ends.size) {
            1 ->
                if (ends[0] == "any") {
                    PortType(-1, -1)
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
        val minPortNumber = 0
        val maxPortNumber = 65535

        val portNumber = port.toInt()
        if (portNumber < minPortNumber || portNumber > maxPortNumber) {
            throw IllegalArgumentException("Port number $portNumber out of bounds ($minPortNumber-$maxPortNumber)")
        }
        return portNumber
    }

    fun parseSubnet(def: String): SubnetType {
        val cleaned = def.clean()
        val parts = cleaned.split(":")
        val (virtualNetworkName, virtualNetworkAddress) = when (parts.size) {
            1 -> Pair(null, parts[0])
            2 -> Pair(parts[0], parts[1])
            else -> throw IllegalArgumentException("Wrong subnet format. use CIDR or VN:CIDR")
        }
        val subnetIP = parseSubnetIP(virtualNetworkAddress)
        val fullNetworkName = if (virtualNetworkName != null) {
            "$virtualNetworkName:$subnetIP"
        } else {
            subnetIP
        }
        val subnetPrefix = parseSubnetPrefix(virtualNetworkAddress)
        return SubnetType(fullNetworkName, subnetPrefix)
    }

    fun parseSubnetIP(virtualNetworkAddress: String): String {
        val minIpPart = 0
        val maxIpPart = 255

        val subnetIP = virtualNetworkAddress.split("/")[0]
        val ipParts = subnetIP.split(".")
        if (ipParts.size != 4) {
            throw IllegalArgumentException("Wrong subnet IP format.")
        }
        if (ipParts[0].toInt() == 0) {
            throw IllegalArgumentException("Wrong subnet IP format.")
        }
        ipParts.map { it.toInt() }.map {
            if (it < minIpPart || it > maxIpPart) {
                throw IllegalArgumentException("Wrong subnet IP format.")
            }
        }
        return subnetIP
    }

    fun parseSubnetPrefix(virtualNetworkAddress: String): Int {
        val minSubnetPrefix = 0
        val maxSubnetPrefix = 32

        val subnetAddressParts = virtualNetworkAddress.split("/")
        val subnetPrefix = when (subnetAddressParts.size) {
            2 -> subnetAddressParts[1].toInt()
            else -> throw IllegalArgumentException("Wrong subnet format. use CIDR or VN:CIDR")
        }
        if (subnetPrefix < minSubnetPrefix || subnetPrefix > maxSubnetPrefix) {
            throw IllegalArgumentException("Subnet prefix $subnetPrefix out of bounds. ($minSubnetPrefix-$maxSubnetPrefix)")
        }
        return subnetPrefix
    }

    fun createAddress(type: String, cidr: String?, network: VirtualNetwork?, policy: NetworkPolicy?): AddressType {
        val subnet = if (type == "CIDR" && cidr != null) parseSubnet(cidr) else null
        val networkName = if (type == "Network" && network != null) network.name else null
        val policyName = if (type == "Policy" && policy != null) policy.name else null
        return AddressType(subnet, networkName, null, policyName)
    }

    fun randomUUID(): String =
        UUID.randomUUID().toString()

    fun ruleToString(rule: PolicyRuleType, index: Int): String {
        return "$index: ${rule.actionList.simpleAction} protocol ${rule.protocol} ${PropertyFormatter.format(rule.srcAddresses[0])} ports ${rule.srcPorts.joinToString(", "){PropertyFormatter.format(it)}} " +
                "${rule.direction} ${PropertyFormatter.format(rule.dstAddresses[0])} ports ${rule.dstPorts.joinToString(", "){PropertyFormatter.format(it)}}"
    }

    fun stringToRule(ruleString: String, policy: NetworkPolicy): PolicyRuleType {
        val index = ruleString.split(":")[0].toInt()
        return policy.entries.policyRule[index]
    }

    fun ruleStringToIndex(ruleString: String): Int {
        return ruleString.split(":")[0].toInt()
    }

    fun lowercase(s: String) =
        s.toLowerCase()

    fun trimMultiline(s: String): String =
        splitMultiline(s).joinToString( "\n" )

    fun splitMultiline(s: String): List<String> =
        s.split('\n').map { it.trim() }.filter { it.isNotBlank() }
}
