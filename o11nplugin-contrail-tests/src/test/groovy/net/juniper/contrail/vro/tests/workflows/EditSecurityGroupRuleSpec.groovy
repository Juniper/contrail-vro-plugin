/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.SecurityGroup
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addRuleToSecurityGroupWorkflowName
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.editRuleOfSecurityGroupWorkflowName

class EditSecurityGroupRuleSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = workflowFromScript(addRuleToSecurityGroupWorkflowName)
    def editSecurityGroupRule = workflowFromScript(editRuleOfSecurityGroupWorkflowName)

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

    def "Editing a security group rule"() {
        given: "A correct set of attributes"
        def securityGroup = dependencies.someSecurityGroup()

        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()

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

        def sizeBeforeEdit = securityGroup.getEntries()?.getPolicyRule()?.size() ?: 0

        when: "Running the script"
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

        then: "The security group is updated with the modified rule"
        1 * connectorMock.update({
            def _it = it as SecurityGroup
            _it.uuid == securityGroup.uuid &&
            _it.entries.policyRule.size() == sizeBeforeEdit &&
            _it.entries.policyRule.any{
                it.ethertype == someDifferentEthertype &&
                it.protocol == someDifferentProtocol
            }}) >> Status.success()
    }
}