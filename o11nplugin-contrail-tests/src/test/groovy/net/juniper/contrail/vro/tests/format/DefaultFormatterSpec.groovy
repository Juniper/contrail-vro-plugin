package net.juniper.contrail.vro.tests.format

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.IpamSubnets
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.vro.format.DefaultFormat
import net.juniper.contrail.vro.format.PropertyFormatter

import static net.juniper.contrail.vro.config.constants.Constants.getTcp

import spock.lang.Specification

class DefaultFormatterSpec extends Specification {
    def propertyFormatter = PropertyFormatter.INSTANCE
    def defaultFormatter = DefaultFormat.INSTANCE
    def someIpPrefix = "1.2.3.4"
    def someIpPrefixLen = 16
    def someSubnetType = new SubnetType(someIpPrefix, someIpPrefixLen)
    def someAddressType = new AddressType(null, null, null, null, [someSubnetType])
    def someActionList = new ActionListType("simpleAction")
    def someProtocol = tcp
    def someDirection = ">"

    def correctlyFormattedAddressType =
        """Subnet: -
        |Virtual Network: -
        |Security Group: -
        |Network Policy: -
        |Subnet List: 
        |    Ip Prefix:  ${someIpPrefix}
        |Ip Prefix Len:  ${someIpPrefixLen}
        |""".stripMargin()

    private class PolicyRuleHolder {
        PolicyRuleType policyRuleType

        PolicyRuleHolder(PolicyRuleType prt) {
            policyRuleType = prt
        }
    }

    def "Formatting an AddressType with a subnet list"() {
        given: "An AddressType object"
        def addressType = someAddressType

        when: "Formatting to a String"
        def formatted = defaultFormatter.format(addressType)

        then: "The string is correct"
        formatted == correctlyFormattedAddressType
    }

    def "Formatting Ipam Subnets class"() {
        given: "An Ipam Subnets Object with a list of Ipam Subnets"
        def someIpamSubnetType = new IpamSubnetType(someSubnetType)
        def someIpamSubnets = new IpamSubnets([someIpamSubnetType])
        def expectedResult = propertyFormatter.format(someSubnetType)

        when: "Formatting to a String"
        def formatted = defaultFormatter.format(someIpamSubnets)

        then: "The resulting string is correct"
        formatted == expectedResult
    }

    def "Formatting PolicyRuleType class"() {
        given: "An object with one field of type PolicyRuleType"
        def somePolicyRule = new PolicyRuleType()
        somePolicyRule.actionList = someActionList
        somePolicyRule.protocol = someProtocol
        somePolicyRule.addDstAddresses(someAddressType)
        somePolicyRule.direction = someDirection

        def obj = new PolicyRuleHolder(somePolicyRule)
        def expectedResult = "${PolicyRuleType.simpleName}: ${propertyFormatter.format(obj.policyRuleType)}"

        when: "Formatting to a String"
        def formatted = defaultFormatter.format(obj)

        then: "The resulting string is correct"
        formatted == expectedResult

    }
}
