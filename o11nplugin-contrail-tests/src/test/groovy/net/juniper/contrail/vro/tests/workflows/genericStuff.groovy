package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper

class SomeSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = engine.getFunctionFromWorkflowScript(workflows, "Add network IPAM to virtual network")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailVnSubnetsType = Java.type('net.juniper.contrail.vro.gen.VnSubnetsType_Wrapper');")
    }

    def mockVirtualNetwork = Mock(VirtualNetwork_Wrapper)
    def mockNetworkIpam = Mock(NetworkIpam_Wrapper)

    def "Adding "() {
        given: "A correct set of attributes"

        when: "Running the script"
        createContext()
        def dependencies = createDependencies()
        loadWrapperTypes()

        engine.invokeFunction(
                addFlatIpamTonetwork,
                mockVirtualNetwork,
                mockNetworkIpam,
        )

        then: "A "
        1 * mockVirtualNetwork.addNetworkIpam(mockNetworkIpam, _)

        and: "The "
        1 * mockVirtualNetwork.update()
    }
}