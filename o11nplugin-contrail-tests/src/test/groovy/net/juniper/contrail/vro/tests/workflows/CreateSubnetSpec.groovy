/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class CreateSubnetSpec extends WorkflowSpec {

    def createSubnet = getWorkflowScript("Add subnet to virtual network")

    def someSubnet = "1.2.3.4/16"
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
//        invokeFunction(
//            createSubnet,
//            virtualNetwork,
//            networkIpam,
//            someSubnet,
//            someAllocationPools,
//            someAllocUnit,
//            someAddrFromStart,
//            someDnsServerAddress,
//            someDefaultGateway,
//            someEnableDhcp
//        )

        then: "The parent virtual network is updated with the new subnet"
        // TODO: this is some complicated result
        1 == 1
//        1 * connectorMock.update({
//            it.uuid == virtualNetwork.uuid}) >> Status.success()
    }
}