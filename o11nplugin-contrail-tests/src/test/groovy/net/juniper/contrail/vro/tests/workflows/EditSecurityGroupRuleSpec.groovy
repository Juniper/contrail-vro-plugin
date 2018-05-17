/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.SecurityGroup
import org.spockframework.mock.MockUtil

class EditSecurityGroupRuleSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = getWorkflowScript("Add rule to security group")
    def editSecurityGroupRule = getWorkflowScript("Edit rule of security group")

    def someDirection = "ingress"
    def someEthertype = "IPv4"
    def someDifferentEthertype = "IPv6"
    def someAddressType = "CIDR"
    def someAddressCidr = "1.2.3.4/16"
    def someAddressSecurityGroup = null
    def someProtocol = "tcp"
    def someDifferentProtocol = "udp"
    def somePorts = "1-2"
    def firstRule = "0: someRuleParams"

    def "Adding "() {
        given: "A correct set of attributes"
        def securityGroup = dependencies.someSecurityGroup()

        connectorMock.read(_) >> Status.success()

        when: "Running the scripts"
        // add a rule to the security group
        invokeFunction(
            addRuleToSecurityGroup,
            securityGroup,
            someDirection,
            someEthertype,
            someAddressType,
            someAddressCidr,
            someAddressSecurityGroup,
            someProtocol,
            somePorts
        )
        // edit the added rule
        invokeFunction(
            editSecurityGroupRule,
            securityGroup,
            firstRule,
            someDirection,
            someDifferentEthertype,
            someAddressType,
            someAddressCidr,
            someAddressSecurityGroup,
            someDifferentProtocol,
            somePorts
        )

        then: "The rule is correctly added to the security group"
        1 * connectorMock.update({
            def _it = it as SecurityGroup
            _it.uuid == securityGroup.uuid &&
            _it.entries.policyRule.any{
                it.ethertype == someEthertype &&
                it.protocol == someProtocol
            }}) >> Status.success()
        then: "The security group is updated with the modified rule"
        1 * connectorMock.update({
            def _it = it as SecurityGroup
            _it.uuid == securityGroup.uuid &&
            _it.entries.policyRule.any{
                it.ethertype == someDifferentEthertype &&
                it.protocol == someDifferentProtocol
            }}) >> Status.success()
    }
}