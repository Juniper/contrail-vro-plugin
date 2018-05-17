/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

class AddPolicyToVirtualNetwork extends WorkflowSpec {

    def addNetworkPolicyToVirtualNetwork = getWorkflowScript("Add network policy to virtual network")

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
        connectorMock.update({
            it.uuid == virtualNetwork.uuid &&
            it.networkPolicy.any {
                it.to == networkPolicy.qualifiedName
            }}) >> Status.success()
    }
}
