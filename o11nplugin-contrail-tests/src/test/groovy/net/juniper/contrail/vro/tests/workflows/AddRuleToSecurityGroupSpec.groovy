/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

class AddRuleToSecurityGroupSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = getWorkflowScript("Add rule to security group")

    def someDirection = "ingress"
    def someEthertype = "IPv4"
    def someAddressType = "CIDR"
    def someAddressCidr = "1.2.3.4/16"
    def someAddressSecurityGroup = null
    def someProtocol = "tcp"
    def somePorts = "1-2"

    def "Adding rule to a security group with existing rule list"() {
        given: "A correct set of attributes"
        def securityGroup = dependencies.someSecurityGroup()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
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

        then: "The parent security group should be updated."
        1 * connectorMock.update({
            it.uuid == securityGroup.uuid &&
            it.entries.policyRule.any{
                // TODO: how to check if a rule was added with 100% certainty?
                it.ethertype == someEthertype &&
                it.protocol == someProtocol
            }}) >> Status.success()
    }
}
