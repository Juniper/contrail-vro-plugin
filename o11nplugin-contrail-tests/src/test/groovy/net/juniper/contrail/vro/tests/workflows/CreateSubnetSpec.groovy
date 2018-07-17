/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.VirtualNetwork

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addSubnetToVirtualNetworkWorkflowName

class CreateSubnetSpec extends WorkflowSpec {

    def createSubnet = workflowFromScript(addSubnetToVirtualNetworkWorkflowName)

    def somePrefix = "1.2.3.4"
    def somePrefixLen = 16
    def someSubnet = "$somePrefix/$somePrefixLen".toString()

    def someAllocationPools = null
    def someAllocUnit = null
    def someAddrFromStart = false
    def someDnsServerAddress = null
    def someDefaultGateway = "1.2.3.4"
    def someEnableDhcp = true

    def "Creating a subnet"() {
        given: "A correct set of attributes"
        def virtualNetwork = dependencies.someVirtualNetwork()
        def networkIpam = dependencies.someNetworkIpam()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            createSubnet,
            virtualNetwork,
            networkIpam,
            someSubnet,
            someAllocationPools,
            someAllocUnit,
            someAddrFromStart,
            someDnsServerAddress,
            someDefaultGateway,
            someEnableDhcp
        )

        then: "The parent virtual network is updated with the new subnet"
        1 * connectorMock.update({
            def _it = it as VirtualNetwork
            _it.uuid == virtualNetwork.uuid &&
            _it.networkIpam.collect { it.attr.ipamSubnets.findAll {it != null} }.flatten().any {
                it.subnet.ipPrefix == somePrefix &&
                it.subnet.ipPrefixLen == somePrefixLen
            }}) >> Status.success()
    }
}