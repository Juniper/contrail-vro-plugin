package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.SecurityGroup

interface SecurityGroupRuleProperties {

    fun SecurityGroup.rulePropertyDirection(
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
    private val utils = Utils()

    override fun SecurityGroup.rulePropertyDirection(
        ruleString: String
    ): String? {
        return determineDirection(ruleString)
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
        return utils.formatSubnet(address.subnet)
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
}