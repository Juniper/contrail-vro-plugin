package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class AddSubnetToNetworkIpamSpec extends WorkflowSpec {

    def addSubnetToNetworkIpam = engine.getFunctionFromWorkflowScript(workflows, "Add subnet to network IPAM")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailIpamSubnetType = Java.type('net.juniper.contrail.vro.gen.IpamSubnetType_Wrapper');")
        engine.engine.eval("var ContrailSubnetType = Java.type('net.juniper.contrail.vro.gen.SubnetType_Wrapper');")
        engine.engine.eval("var ContrailAllocationPoolType = Java.type('net.juniper.contrail.vro.gen.AllocationPoolType_Wrapper');")
        engine.engine.eval("var ContrailIpamSubnets = Java.type('net.juniper.contrail.vro.gen.IpamSubnets_Wrapper');")
    }

    def mockUtil = new MockUtil()

    def someSubnet = "1.2.3.4/16"
    def someAllocationPools = null
    def someAllocUnit = null
    def someAddrFromStart = false
    def someDnsServerAddress = null
    def someDefaultGateway = "1.2.3.4"
    def someEnableDhcp = true

    def "Adding a subnet to network IPAM"() {
        given: "A correct set of attributes"
        createContext()
        def dependencies = createDependencies()
        def networkIpam = dependencies.someNetworkIpam()

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()
        engine.invokeFunction(
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
        1 * DetachedMocksKt.apiConnectorMock.update({
            it.uuid == networkIpam.uuid &&
            it.ipamSubnets.subnets.any{
                it.defaultGateway == someDefaultGateway
            }}) >> Status.success()
    }
}
