/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

class AddSubnetToNetworkIpamSpec extends WorkflowSpec {
    def addSubnetToNetworkIpam = getWorkflowScript("Add subnet to network IPAM")

    def someSubnet = "1.2.3.4/16"
    def someAllocationPools = null
    def someAllocUnit = null
    def someAddrFromStart = false
    def someDnsServerAddress = null
    def someDefaultGateway = "1.2.3.4"
    def someEnableDhcp = true

    def "Adding a subnet to network IPAM"() {
        given: "A correct set of attributes"
        def networkIpam = dependencies.someNetworkIpam()
        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addSubnetToNetworkIpam,
            networkIpam,
            someSubnet,
            someAllocationPools,
            someAllocUnit,
            someAddrFromStart,
            someDnsServerAddress,
            someDefaultGateway,
            someEnableDhcp
        )

        then: "The parent Network IPAM should be updated."
        1 * connectorMock.update({
            it.uuid == networkIpam.uuid &&
            it.ipamSubnets.subnets.any{
                it.defaultGateway == someDefaultGateway
            }}) >> Status.success()
    }
}
