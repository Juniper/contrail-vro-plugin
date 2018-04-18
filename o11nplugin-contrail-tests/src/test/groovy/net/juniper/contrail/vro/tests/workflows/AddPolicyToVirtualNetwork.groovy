/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper

class AddPolicyToVirtualNetwork extends WorkflowSpec {

    def addRuleToSecurityGroup = engine.getFunctionFromWorkflowScript(workflows, "Add network policy to virtual network")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailSequenceType = Java.type('net.juniper.contrail.vro.gen.SequenceType_Wrapper');")
        engine.engine.eval("var ContrailVirtualNetworkPolicyType = Java.type('net.juniper.contrail.vro.gen.VirtualNetworkPolicyType_Wrapper');")
    }

    def mockVirtualNetwork = Mock(VirtualNetwork_Wrapper)
    def mockNetworkPolicy = Mock(NetworkPolicy_Wrapper)

    def "Adding a network policy to a virtual network"() {
        given: "A correct set of attributes"
        mockVirtualNetwork.networkPolicy >> []

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addRuleToSecurityGroup,
            mockVirtualNetwork,
            mockNetworkPolicy
        )

        then: "A network policy is added to the network"
        1 * mockVirtualNetwork.addNetworkPolicy(_, _)

        and: "The virtual network object is updated"
        1 * mockVirtualNetwork.update()
    }
}
