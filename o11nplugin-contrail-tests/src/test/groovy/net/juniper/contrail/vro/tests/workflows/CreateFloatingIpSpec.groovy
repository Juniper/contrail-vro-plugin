package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.gen.FloatingIpPool_Wrapper
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.Project_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import org.spockframework.mock.MockUtil

class CreateFloatingIpSpec extends WorkflowSpec {

    def createFloatingIp = engine.getFunctionFromWorkflowScript(workflows, "Create floating IP")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailFloatingIp = Java.type('net.juniper.contrail.vro.gen.FloatingIp_Wrapper');")
    }

    def mockUtil = new MockUtil()

    def "Creating floating IP"() {
        given: "A correct set of attributes"
        def dependencies = createContextAndDependencies()
        def parent = dependencies.someFloatingIpPool()
        def someProjects = null
        def someAddress = null

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()

        engine.invokeFunction(
            createFloatingIp,
            parent,
            someProjects,
            someAddress
        )

        then: "A floating IP with given parameters is created"
        1 * DetachedMocksKt.apiConnectorMock.create({it.parentUuid == parent.uuid}) >> Status.success()
        DetachedMocksKt.apiConnectorMock.create(_) >> Status.success()
    }
}
