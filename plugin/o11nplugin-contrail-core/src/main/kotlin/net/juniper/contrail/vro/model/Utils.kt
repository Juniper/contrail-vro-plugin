/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.PortType
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import java.util.UUID

class Utils {
    private fun String.clean() =
        replace("\\s+", "")

    fun parsePorts(ports: String): List<PortType> {
        val cleaned = ports.clean()
        val intervals = cleaned.split(",")
        return intervals.map { parsePort(it) }
    }

    private fun parsePort(def: String): PortType {
        val ends = def.split("-")
        return when (ends.size) {
            1 -> PortType(ends[0].toInt(), ends[0].toInt())
            2 -> PortType(ends[0].toInt(), ends[1].toInt())
            else -> PortType()
        }
    }

    fun parseSubnet(def: String): SubnetType {
        val cleaned = def.clean()
        val parts = cleaned.split("/")
        return SubnetType(parts[0], parts[1].toInt())
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
        return "$index: ${rule.actionList.simpleAction} ${rule.direction} ${rule.ruleUuid}"
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
}
