/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkIpam

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addSubnetToNetworkIpamWorkflowName

class AddSubnetToNetworkIpamSpec extends WorkflowSpec {

    def addSubnetToNetworkIpam = workflowFromScript(addSubnetToNetworkIpamWorkflowName)

    def somePrefix = "1.2.3.4"
    def somePrefixLen = 16
    def someSubnet = "$somePrefix/$somePrefixLen".toString()
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
            def _it = it as NetworkIpam
            _it.uuid == networkIpam.uuid &&
            _it.ipamSubnets.subnets.any{
                it.subnet.ipPrefix == somePrefix &&
                it.subnet.ipPrefixLen == somePrefixLen &&
                it.defaultGateway == someDefaultGateway
            }}) >> Status.success()
    }
}
