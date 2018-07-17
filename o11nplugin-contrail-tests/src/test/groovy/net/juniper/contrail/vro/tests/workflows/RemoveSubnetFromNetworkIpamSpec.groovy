/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkIpam

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addSubnetToNetworkIpamWorkflowName
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.removeSubnetFromNetworkIpamWorkflowName

class RemoveSubnetFromNetworkIpamSpec extends WorkflowSpec {

    def addSubnetToNetworkIpam = workflowFromScript(addSubnetToNetworkIpamWorkflowName)
    def removeSubnetFromNetworkIpam = workflowFromScript(removeSubnetFromNetworkIpamWorkflowName)

    def somePrefix = "1.2.3.4"
    def somePrefixLen = 16
    def someSubnet = "$somePrefix/$somePrefixLen".toString()
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
        connectorMock.update(_) >> Status.success()
        // add a subnet to the network IPAM
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

        when: "Running the script"
        invokeFunction(
            removeSubnetFromNetworkIpam,
            networkIpam,
            someSubnet
        )

        then: "The parent Network IPAM should be updated without the new subnet."
        1 * connectorMock.update({
            def _it = it as NetworkIpam
            _it.uuid == networkIpam.uuid &&
            !_it.ipamSubnets.subnets.any{
                it.subnet.ipPrefix == somePrefix &&
                it.subnet.ipPrefixLen == somePrefixLen
            }}) >> Status.success()
    }
}