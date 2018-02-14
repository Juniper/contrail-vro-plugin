/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.PortType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.Subnet
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

    fun isValidPool(input: List<String>): Boolean =
        NetAddressValidator.areValidPools(input)

    fun isValidIpv4Pool(input: List<String>): Boolean =
        NetAddressValidator.IPv4.areValidPools(input)

    fun isValidIpv6Pool(input: List<String>): Boolean =
        NetAddressValidator.IPv6.areValidPools(input)

    fun isValidAllocationPool(cidr: String, pools: List<String>): Boolean {
        if (isValidIpv4Cidr(cidr) && isValidIpv4Pool(pools)) {
            return parsePools(cidr, pools, ::IPv4)
        } else if (isValidIpv6Cidr(cidr) && isValidIpv6Pool(pools)) {
            return parsePools(cidr, pools, ::IPv6)
        }
        return false
    }

    fun isInCidr(cidr: String, address: String): Boolean {
        if (isValidIpv4Address(address) && isValidIpv4Cidr(cidr)) {
            return IPv4(address) in getSubnetRange(cidr, ::IPv4)
        } else if (isValidIpv6Address(address) && isValidIpv6Cidr(cidr)) {
            return IPv6(address) in getSubnetRange(cidr, ::IPv6)
        }
        return false
    }

    fun isFree(cidr: String, address: String, pools: List<String>?, dnsAddr: String?): Boolean {
        if (isValidIpv4Cidr(cidr) && isValidIpv4Address(address)) {
            if (pools != null && !pools.isBlankList() && !isValidIpv4Pool(pools)) return false
            if (dnsAddr != null && dnsAddr.isNotBlank() && !isValidIpv4Address(dnsAddr)) return false
            val ip = IPv4(address)
            return (ip in getSubnetRange(cidr, ::IPv4)) && ip.notInPools(pools, ::IPv4)
                    && !dnsAddr.equalsIp(ip, ::IPv4)
        } else if (isValidIpv6Cidr(cidr) && isValidIpv6Address(address)) {
            if (pools != null && !pools.isBlankList() && !isValidIpv6Pool(pools)) return false
            if (dnsAddr != null && dnsAddr.isNotBlank() && !isValidIpv6Address(dnsAddr)) return false
            val ip = IPv6(address)
            return (ip in getSubnetRange(cidr, ::IPv6)) && ip.notInPools(pools, ::IPv6)
                    && !dnsAddr.equalsIp(ip, ::IPv6)
        }
        return false
    }

    fun getVnSubnet(network: VirtualNetwork, ipam: NetworkIpam): VnSubnetsType =
        network.networkIpam?.find { it.uuid == ipam.uuid }?.attr ?: VnSubnetsType()

    fun isNetworRelatedToIpam(network: VirtualNetwork, ipam: NetworkIpam): Boolean =
        network.networkIpam?.any { it.uuid == ipam.uuid } ?: false

    fun removeSubnetFromVirtualNetwork(network: VirtualNetwork, subnet: Subnet) {
        val ipams = network.networkIpam ?: return
        //first remove subnet from attributes
        ipams.forEach {
            it.attr.ipamSubnets.removeIf { it.subnetUuid == subnet.uuid }
        }
        //then remove IPAMs if have not subnets
        ipams.removeIf {
            it.attr.ipamSubnets.isEmpty()
        }
    }

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

    fun createAddress(
        type: String,
        cidr: String?,
        network: VirtualNetwork?,
        policy: NetworkPolicy?,
        securityGroup: SecurityGroup?
    ): AddressType {
        val subnet = if (type == "CIDR" && cidr != null) parseSubnet(cidr) else null
        val networkName = if (type == "Network" && network != null) network.name else null
        val policyName = if (type == "Policy" && policy != null) policy.name else null
        val securityGroupName = if (type == "Security Group") {
            securityGroup?.qualifiedName?.joinToString(":") ?: "local"
        } else null
        return AddressType(subnet, networkName, securityGroupName, policyName)
    }

    fun randomUUID(): String =
        UUID.randomUUID().toString()

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

    fun ruleStringToIndex(ruleString: String): Int {
        return ruleString.split(":")[0].toInt()
    }

    fun lowercase(s: String) =
        s.toLowerCase()

    fun trimList(s: List<String>) : List<String> =
        s.trimList()

    fun isBlankList(s: List<String>?) : Boolean =
        s.isBlankList()
}
