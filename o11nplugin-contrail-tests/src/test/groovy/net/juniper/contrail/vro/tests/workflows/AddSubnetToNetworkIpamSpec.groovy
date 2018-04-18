package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.IpamSubnets_Wrapper
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper

class AddSubnetToNetworkIpamSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = engine.getFunctionFromWorkflowScript(workflows, "Add subnet to network IPAM")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailIpamSubnetType = Java.type('net.juniper.contrail.vro.gen.IpamSubnetType_Wrapper');")
        engine.engine.eval("var ContrailSubnetType = Java.type('net.juniper.contrail.vro.gen.SubnetType_Wrapper');")
        engine.engine.eval("var ContrailAllocationPoolType = Java.type('net.juniper.contrail.vro.gen.AllocationPoolType_Wrapper');")
        engine.engine.eval("var ContrailIpamSubnets = Java.type('net.juniper.contrail.vro.gen.IpamSubnets_Wrapper');")
    }

    def mockParent = Mock(NetworkIpam_Wrapper)
    def mockSubnets = Mock(IpamSubnets_Wrapper)
    def someSubnet = "1.2.3.4/16"
    def someAllocationPools = null
    def someAllocUnit = null
    def someAddrFromStart = false
    def someDnsServerAddress = null
    def someDefaultGateway = "1.2.3.4"
    def someEnableDhcp = true

    def "Adding a subnet to network IPAM"() {
        given: "A correct set of attributes"
        mockParent.getIpamSubnets() >> mockSubnets

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addFlatIpamTonetwork,
            mockParent,
            someSubnet,
            someAllocationPools,
            someAllocUnit,
            someAddrFromStart,
            someDnsServerAddress,
            someDefaultGateway,
            someEnableDhcp
        )

        then: "A subnet is added to parent's subnets"
        1 * mockSubnets.addSubnets({
            it.defaultGateway == someDefaultGateway
        })

        and: "The parent Network IPAM should be updated."
        1 * mockParent.update()
    }
}
