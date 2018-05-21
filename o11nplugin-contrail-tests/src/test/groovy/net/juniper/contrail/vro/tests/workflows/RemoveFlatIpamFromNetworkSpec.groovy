/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.VirtualNetwork
import org.spockframework.mock.MockUtil

class RemoveFlatIpamFromNetworkSpec extends WorkflowSpec {

    def addFlatIpamToNetwork = getWorkflowScript("Add network IPAM to virtual network")
    def removeFlatIpamFromNetwork = getWorkflowScript("Remove network IPAM from virtual network")

    def "Removing a flat IPAM from a network"() {
        given: "A correct set of attributes"
        def virtualNetwork = dependencies.someVirtualNetwork()
        def networkIpam = dependencies.someNetworkIpam()

        connectorMock.read(_) >> Status.success()

        when: "Running the scripts"
        invokeFunction(
            addFlatIpamToNetwork,
            virtualNetwork,
            networkIpam
        )
        invokeFunction(
            removeFlatIpamFromNetwork,
            virtualNetwork,
            networkIpam
        )

        then: "A network IPAM is added to the network"
        1 * connectorMock.update({
            def _it = it as VirtualNetwork
            _it.uuid == virtualNetwork.uuid &&
            _it.networkIpam.any{
                it.to == networkIpam.qualifiedName
            }}) >> Status.success()
        then: "The network is updated without the new IPAM"
        1 * connectorMock.update({
            def _it = it as VirtualNetwork
            _it.uuid == virtualNetwork.uuid &&
            !_it.networkIpam.any{
                it.to == networkIpam.qualifiedName
            }}) >> Status.success()
    }
}