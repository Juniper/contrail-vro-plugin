/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.VirtualNetwork
import org.spockframework.mock.MockUtil

class DeleteSubnetSpec extends WorkflowSpec {

    def deleteSubnet = getWorkflowScript("Remove subnet from virtual network")

    def subnetPrefix = "1.2.3.4"
    def subnetPrefixLen = 16

    def "Removing a subnet from a virtual network"() {
        given: "A correct set of attributes"
        def virtualNetwork = dependencies.someVirtualNetworkWithSubnet("$subnetPrefix/$subnetPrefixLen")
        connectorMock.read(_) >> Status.success()

        assert virtualNetwork.subnets().any {
            it.subnet.ipPrefix == subnetPrefix &&
                it.subnet.ipPrefixLen == subnetPrefixLen
        }

        when: "Running the script"
        invokeFunction(
            deleteSubnet,
            virtualNetwork,
            "$subnetPrefix/$subnetPrefixLen"
        )

        then: "The virtual network is updated and does not contain the deleted subnet"
        1 * connectorMock.update({
            def _it = it as VirtualNetwork
            _it.uuid == virtualNetwork.uuid &&
            (_it.networkIpam == null ||
            !_it.networkIpam.collect { it.attr.ipamSubnets.findAll {it != null} }.flatten().any {
                it.subnet.ipPrefix == subnetPrefix &&
                it.subnet.ipPrefixLen == subnetPrefixLen
            })
            }) >> Status.success()
    }
}