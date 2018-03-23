/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper

class AddRuleToSecurityGroupSpec extends WorkflowSpec {

    def addRuleToSecurityGroup = engine.getFunctionFromWorkflowScript(workflows, "Add rule to security group")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailPolicyRuleType = Java.type('net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper');")
        engine.engine.eval("var ContrailPolicyEntriesType = Java.type('net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper');")
    }

    def mockParent = Mock(SecurityGroup_Wrapper)
    def mockEntries = Mock(PolicyEntriesType_Wrapper)

    def someDirection = "ingress"
    def someEthertype = "IPv4"
    def someAddressType = "CIDR"
    def someAddressCidr = "1.2.3.4/16"
    def someAddressSecurityGroup = null
    def someProtocol = "tcp"
    def somePorts = "1-2"

    def "Adding rule to a security group with existing rule list"() {
        given: "A correct set of attributes"
        mockParent.getEntries() >> mockEntries

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addRuleToSecurityGroup,
            mockParent,
            someDirection,
            someEthertype,
            someAddressType,
            someAddressCidr,
            someAddressSecurityGroup,
            someProtocol,
            somePorts
        )

        // There is no way to mock the constructor, so checking if constructor is invoked with correct parameters is hard.
        then: "A new rule entry matching the input parameters should be added to the parent security group."
        1 * mockEntries.addPolicyRule({
            it.ethertype == someEthertype &&
            it.protocol == someProtocol})

        and: "The parent security group should be updated."
        1 * mockParent.update()
    }
}
