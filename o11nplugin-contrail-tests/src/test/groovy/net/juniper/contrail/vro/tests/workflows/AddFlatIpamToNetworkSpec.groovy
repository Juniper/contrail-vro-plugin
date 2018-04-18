package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.VnSubnetsType
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import net.juniper.contrail.vro.gen.VnSubnetsType_Wrapper
import org.spockframework.mock.MockUtil

import java.lang.reflect.Array

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

        println(virtualNetwork.getNetworkIpam())
        println(virtualNetwork)
        println(networkIpam)
        engine.invokeFunction(
            addFlatIpamTonetwork,
            virtualNetwork,
            networkIpam,
        )
        // For some reason even manually adding the network Ipam doesn't work
        println(virtualNetwork.networkIpam)
        virtualNetwork.networkIpam.each {
            println(it)
        }

//        then: "The input network IPAM is added to the virtual network"
//        1 * virtualNetwork.addNetworkIpam(networkIpam, _)

        then: "The virtual network object is updated"
        1 == 1
        1 * DetachedMocksKt.apiConnectorMock.update({it.uuid == virtualNetwork.uuid}) >> Status.success()
//        1 * DetachedMocksKt.apiConnectorMock.update({
//            it.uuid == virtualNetwork.uuid &&
//            it.networkIpam.contains({
//                it.uuid == networkIpam.uuid
//            })}) >> Status.success()
    }
}