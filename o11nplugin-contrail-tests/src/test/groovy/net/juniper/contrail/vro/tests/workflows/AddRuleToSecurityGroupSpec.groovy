/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.SecurityGroup

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addRuleToSecurityGroupWorkflowName

class AddRuleToSecurityGroupSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = workflowFromScript(addRuleToSecurityGroupWorkflowName)

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

        def initialSize = securityGroup.getEntries()?.getPolicyRule()?.size() ?: 0
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
            def _it = it as SecurityGroup
            _it.uuid == securityGroup.uuid &&
            _it.entries.policyRule.size() == initialSize + 1 &&
            _it.entries.policyRule.any{
                it.ethertype == someEthertype &&
                it.protocol == someProtocol
            }}) >> Status.success()
    }
}
