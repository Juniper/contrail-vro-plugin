/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

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
            it.uuid == virtualNetwork.uuid &&
            it.networkIpam.any{
                it.to == networkIpam.qualifiedName
            }}) >> Status.success()
    }
}