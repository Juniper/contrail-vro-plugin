package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.tests.workflows.WorkflowSpec

import static net.juniper.contrail.vro.config.Actions.virtualNetworkSubnets
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addSubnetToVirtualNetworkWorkflowName

class VirtualNetworkSubnetsSpec extends WorkflowSpec {
    def action = actionFromScript(virtualNetworkSubnets)

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

    def "null virtual network group results in null" () {
        given: "null virtual network"
        def network = null

        when: "retrieved subnet list"
        def result = invokeAction(action, network)

        then: "resulting list is null"
        result == null
    }

    def "empty virtual network results in empty list" () {
        given: "empty virtual network"
        def network = dependencies.someVirtualNetwork()

        when: "retrieved subnet list"
        def result = invokeAction(action, network) as List

        then: "resulting list is empty"
        result.isEmpty()
    }

    def "virtual network with single subnet results in list with single formatted subnet" () {
        given: "virtual network with single subnet"
        def network = dependencies.someVirtualNetwork()
        def ipam = dependencies.someNetworkIpam()
        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()

        invokeFunction(
            createSubnet,
            network,
            ipam,
            someSubnet,
            someAllocationPools,
            someAllocUnit,
            someAddrFromStart,
            someDnsServerAddress,
            someDefaultGateway,
            someEnableDhcp
        )

        when: "retrieved subnet list"
        def result = invokeAction(action, network) as List<String>

        then: "resulting list has one formatted element"
        result.size() == 1
        result[0] == someSubnet
    }
}
