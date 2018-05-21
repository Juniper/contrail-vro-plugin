/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkIpam
import org.spockframework.mock.MockUtil

class RemoveSubnetFromNetworkIpamSpec extends WorkflowSpec {

    def addSubnetToNetworkIpam = getWorkflowScript("Add subnet to network IPAM")
    def removeSubnetFromNetworkIpam = getWorkflowScript("Remove network IPAM subnet")

    def someSubnet = "1.2.3.4/16"
    def someAllocationPools = null
    def someAllocUnit = null
    def someAddrFromStart = false
    def someDnsServerAddress = null
    def someDefaultGateway = "1.2.3.4"
    def someEnableDhcp = true

    def "Removing a subnet from IPAM subnet"() {
        given: "A correct set of attributes"
        def networkIpam = dependencies.someNetworkIpam()
        connectorMock.read(_) >> Status.success()

        when: "Running the scripts"
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
        invokeFunction(
            removeSubnetFromNetworkIpam,
            networkIpam,
            someSubnet
        )

        then: "The parent Network IPAM should be updated with a new subnet."
        1 * connectorMock.update({
            def _it = it as NetworkIpam
            _it.uuid == networkIpam.uuid &&
            _it.ipamSubnets.subnets.any{
                it.subnet.ipPrefix + "/" + it.subnet.ipPrefixLen == someSubnet &&
                it.defaultGateway == someDefaultGateway
            }}) >> Status.success()

        then: "The parent Network IPAM should be updated without the new subnet."
        1 * connectorMock.update({
            def _it = it as NetworkIpam
            _it.uuid == networkIpam.uuid &&
            !_it.ipamSubnets.subnets.any{
                it.subnet.ipPrefix + "/" + it.subnet.ipPrefixLen == someSubnet &&
                it.defaultGateway == someDefaultGateway
            }}) >> Status.success()
    }
}