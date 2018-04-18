package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.gen.FloatingIpPool_Wrapper
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import org.spockframework.mock.MockUtil

class CreateFloatingIpSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = engine.getFunctionFromWorkflowScript(workflows, "Create floating IP")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailFloatingIp = Java.type('net.juniper.contrail.vro.gen.FloatingIp_Wrapper');")
    }

    def mockUtil = new MockUtil()
    def mockParent = Mock(FloatingIpPool_Wrapper)
    def parentUuid = "parentUuid"
    def someProjects = null
    def someAddress = null

    def "Creating floating IP"() {
        given: "A correct set of attributes"

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()
        mockParent.internalId >> Sid.empty().with("FloatingIpPool", "1234567890")
        mockParent.uuid >> parentUuid

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addFlatIpamTonetwork,
            mockParent,
            someProjects,
            someAddress
        )

        then: "A floating IP with given parameters is created"
        1 * DetachedMocksKt.apiConnectorMock.create() >> Status.success()
//        1 * DetachedMocksKt.apiConnectorMock.create({it.parentUuid == parentUuid}) >> Status.success()
    }
}
