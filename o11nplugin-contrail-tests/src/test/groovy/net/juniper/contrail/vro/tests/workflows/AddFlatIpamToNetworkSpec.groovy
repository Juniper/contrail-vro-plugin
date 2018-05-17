/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.VirtualNetwork

class AddFlatIpamToNetworkSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = getWorkflowScript("Add network IPAM to virtual network")

    def "Adding a network IPAM to a virtual network"() {
        given: "A correct set of attributes"
        def virtualNetwork = dependencies.someVirtualNetwork()
        def networkIpam = dependencies.someNetworkIpam()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addFlatIpamTonetwork,
            virtualNetwork,
            networkIpam,
        )

        then: "The virtual network object is updated"
        1 * connectorMock.update({
            def _it = it as VirtualNetwork
            _it.uuid == virtualNetwork.uuid &&
            _it.networkIpam.any{
                it.to == networkIpam.qualifiedName
            }}) >> Status.success()
    }
}