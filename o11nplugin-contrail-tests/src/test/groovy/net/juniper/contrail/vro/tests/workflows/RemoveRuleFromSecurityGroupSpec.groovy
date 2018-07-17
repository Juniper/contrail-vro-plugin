/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.SecurityGroup
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addRuleToSecurityGroupWorkflowName
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.removeRuleFromSecurityGroupWorkflowName

class RemoveRuleFromSecurityGroupSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = workflowFromScript(addRuleToSecurityGroupWorkflowName)
    def removeRuleFromSecurityGroup = workflowFromScript(removeRuleFromSecurityGroupWorkflowName)

    def someDirection = "ingress"
    def someEthertype = "IPv4"
    def someAddressType = "CIDR"
    def someAddressCidr = "1.2.3.4/16"
    def someAddressSecurityGroup = null
    def someProtocol = "tcp"
    def somePorts = "1-2"

    def "Removing a rule from a security group"() {
        given: "A correct set of attributes"
        def securityGroup = dependencies.someSecurityGroup()

        def initialSize = securityGroup.getEntries()?.getPolicyRule()?.size() ?: 0
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
        def lastRule = "$initialSize: someRuleParams"

        when: "Running the script"
        invokeFunction(
            removeRuleFromSecurityGroup,
            securityGroup,
            lastRule
        )

        then: "The security group is updated without the new rule"
        1 * connectorMock.update({
            def _it = it as SecurityGroup
            _it.uuid == securityGroup.uuid &&
            _it.entries.policyRule.size() == initialSize &&
            !_it.entries.policyRule.any{
                it.ethertype == someEthertype &&
                it.protocol == someProtocol
            }}) >> Status.success()
    }
}