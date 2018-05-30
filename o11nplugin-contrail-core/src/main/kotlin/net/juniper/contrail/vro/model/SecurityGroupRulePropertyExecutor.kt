/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.config.constants.egress
import net.juniper.contrail.vro.config.constants.ingress

interface SecurityGroupRuleProperties {

    fun SecurityGroup.rulePropertyDirection(
        ruleString: String
    ): String?

    fun SecurityGroup.rulePropertyProtocol(
        ruleString: String
    ): String?

    fun SecurityGroup.rulePropertyEthertype(
        ruleString: String
    ): String?

    fun SecurityGroup.rulePropertyAddressType(
        ruleString: String
    ): String?

    fun SecurityGroup.rulePropertyAddressCidr(
        ruleString: String
    ): String?

    fun SecurityGroup.rulePropertyAddressSecurityGroup(
        ruleString: String
    ): SecurityGroup?

    fun SecurityGroup.rulePropertyPorts(
        ruleString: String
    ): String?
}

class SecurityGroupRulePropertyExecutor(private val connection: Connection) : SecurityGroupRuleProperties {

    override fun SecurityGroup.rulePropertyDirection(
        ruleString: String
    ): String? {
        return determineDirection(ruleString)
    }

    override fun SecurityGroup.rulePropertyProtocol(
        ruleString: String
    ): String? {
        val rule = extractRule(ruleString)
        return rule.protocol
    }

    override fun SecurityGroup.rulePropertyEthertype(
        ruleString: String
    ): String? {
        val rule = extractRule(ruleString)
        return rule.ethertype
    }

    override fun SecurityGroup.rulePropertyAddressType(
        ruleString: String
    ): String? {
        val address = extractAddress(ruleString)
        return utils.addressType(address)
    }

    override fun SecurityGroup.rulePropertyAddressCidr(
        ruleString: String
    ): String? {
        val address = extractAddress(ruleString)
        return utils.subnetToString(address.subnet)
    }

    override fun SecurityGroup.rulePropertyAddressSecurityGroup(
        ruleString: String
    ): SecurityGroup? {
        val address = extractAddress(ruleString)
        return connection.findByFQN(address.securityGroup)
    }

    override fun SecurityGroup.rulePropertyPorts(
        ruleString: String
    ): String? {
        val direction = determineDirection(ruleString)
        val rule = extractRule(ruleString)
        val ports = if (direction == ingress) rule.srcPorts else rule.dstPorts
        return utils.formatPorts(ports)
    }

    private fun SecurityGroup.extractAddress(
        ruleString: String
    ): AddressType {
        val direction = determineDirection(ruleString)
        val rule = extractRule(ruleString)
        val addresses = if (direction == ingress) rule.srcAddresses else rule.dstAddresses
        return addresses[0]
    }

    private fun SecurityGroup.determineDirection(
        ruleString: String
    ): String? {
        val rule = extractRule(ruleString)
        val sourceAddress = rule.srcAddresses[0]
        return if (sourceAddress.isLocal) egress else ingress
    }

    private val AddressType.isLocal get(): Boolean =
        securityGroup == "local"

    private fun SecurityGroup.extractRule(
        ruleString: String
    ): PolicyRuleType {
        val ruleIndex = utils.stringToIndex(ruleString)
        return entries.policyRule[ruleIndex]
    }
}