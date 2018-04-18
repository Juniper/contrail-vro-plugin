package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class AddFlatIpamToNetworkSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = engine.getFunctionFromWorkflowScript(workflows, "Add network IPAM to virtual network")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailVnSubnetsType = Java.type('net.juniper.contrail.vro.gen.VnSubnetsType_Wrapper');")
    }

    def mockUtil = new MockUtil()

    def "Adding a network IPAM to a virtual network"() {
        given: "A correct set of attributes"
        def dependencies = createContextAndDependencies()
        def virtualNetwork = dependencies.someVirtualNetwork()
        def networkIpam = dependencies.someNetworkIpam()

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()
        engine.invokeFunction(
            addFlatIpamTonetwork,
            virtualNetwork,
            networkIpam,
        )

        then: "The virtual network object is updated"
        1 * DetachedMocksKt.apiConnectorMock.update({
            it.uuid == virtualNetwork.uuid &&
            it.networkIpam.any{
                it.to == networkIpam.qualifiedName
            }}) >> Status.success()
    }
}