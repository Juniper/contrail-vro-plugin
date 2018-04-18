/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import org.spockframework.mock.MockUtil

class AddPolicyToVirtualNetwork extends WorkflowSpec {

    def addNetworkPolicyToVirtualNetwork = engine.getFunctionFromWorkflowScript(workflows, "Add network policy to virtual network")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailSequenceType = Java.type('net.juniper.contrail.vro.gen.SequenceType_Wrapper');")
        engine.engine.eval("var ContrailVirtualNetworkPolicyType = Java.type('net.juniper.contrail.vro.gen.VirtualNetworkPolicyType_Wrapper');")
    }

    def mockUtil = new MockUtil()

    def "Adding a network policy to a virtual network"() {
        given: "A correct set of attributes"
        def dependencies = createContextAndDependencies()
        def virtualNetwork = dependencies.someVirtualNetwork()
        def networkPolicy = dependencies.someNetworkPolicy()

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()

        println(virtualNetwork.networkPolicy)

        engine.invokeFunction(
            addNetworkPolicyToVirtualNetwork,
            virtualNetwork,
            networkPolicy
        )

        println(virtualNetwork.networkPolicy)

        then: "The virtual network object is updated"
        DetachedMocksKt.apiConnectorMock.update({it.uuid == virtualNetwork.uuid}) >> Status.success()
    }
}
