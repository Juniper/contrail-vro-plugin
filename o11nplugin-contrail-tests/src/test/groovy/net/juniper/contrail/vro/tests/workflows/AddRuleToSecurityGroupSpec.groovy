/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class AddRuleToSecurityGroupSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = engine.getFunctionFromWorkflowScript(workflows, "Add rule to security group")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailPolicyRuleType = Java.type('net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper');")
        engine.engine.eval("var ContrailPolicyEntriesType = Java.type('net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper');")
    }

    def mockUtil = new MockUtil()

    def someDirection = "ingress"
    def someEthertype = "IPv4"
    def someAddressType = "CIDR"
    def someAddressCidr = "1.2.3.4/16"
    def someAddressSecurityGroup = null
    def someProtocol = "tcp"
    def somePorts = "1-2"

    def "Adding rule to a security group with existing rule list"() {
        given: "A correct set of attributes"
        def dependencies = createContextAndDependencies()
        def securityGroup = dependencies.someSecurityGroup()

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()

        engine.invokeFunction(
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

        // TODO: check that the rule was added

        then: "The parent security group should be updated."
        1 * DetachedMocksKt.apiConnectorMock.update({it.uuid == securityGroup.uuid}) >> Status.success()
    }
}
