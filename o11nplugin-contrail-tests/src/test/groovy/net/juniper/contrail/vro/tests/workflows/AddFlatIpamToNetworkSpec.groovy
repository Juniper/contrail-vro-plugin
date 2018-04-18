package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper

class AddFlatIpamToNetworkSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = engine.getFunctionFromWorkflowScript(workflows, "Add network IPAM to virtual network")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailVnSubnetsType = Java.type('net.juniper.contrail.vro.gen.VnSubnetsType_Wrapper');")
    }

    def mockVirtualNetwork = Mock(VirtualNetwork_Wrapper)
    def mockNetworkIpam = Mock(NetworkIpam_Wrapper)

    def "Adding a network IPAM to a virtual network"() {
        given: "A correct set of attributes"

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addFlatIpamTonetwork,
            mockVirtualNetwork,
            mockNetworkIpam,
        )

        then: "The input network IPAM is added to the virtual network"
        1 * mockVirtualNetwork.addNetworkIpam(mockNetworkIpam, _)

        and: "The virtual network object is updated"
        1 * mockVirtualNetwork.update()
    }
}