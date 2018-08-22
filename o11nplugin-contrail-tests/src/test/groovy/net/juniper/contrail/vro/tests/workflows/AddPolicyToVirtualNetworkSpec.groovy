/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.VirtualNetwork

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddPolicyToVirtualNetworkSpec extends WorkflowSpec {

    def addNetworkPolicyToVirtualNetwork = workflowFromScript(addRelationWorkflowName(VirtualNetwork, NetworkPolicy))

    def "Adding a network policy to a virtual network"() {
        given: "A correct set of attributes"
        def virtualNetwork = dependencies.someVirtualNetwork()
        def networkPolicy = dependencies.someNetworkPolicy()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addNetworkPolicyToVirtualNetwork,
            virtualNetwork,
            networkPolicy
        )

        then: "The virtual network object is updated"
        1 * connectorMock.update({
            def _it = it as VirtualNetwork
            _it.uuid == virtualNetwork.uuid &&
            _it.networkPolicy.any {
                it.to == networkPolicy.qualifiedName
            }}) >> Status.success()
    }
}
