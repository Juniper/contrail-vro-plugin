package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.gen.ServiceInstance_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterface_Wrapper
import org.spockframework.mock.MockUtil

// TODO: PortTuple jest tworzony normalnie przez `new`; odpala się na nim `.create()` i nie działa bo nie znajduje connection
class AddPortTupleToServiceInstanceSpec extends WorkflowSpec {

    def addFlatIpamTonetwork = engine.getFunctionFromWorkflowScript(workflows, "Add port tuple to service instance")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailVirtualMachineInterfacePropertiesType = Java.type('net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper');")
        engine.engine.eval("var ContrailPortTuple = Java.type('net.juniper.contrail.vro.gen.PortTuple_Wrapper');")
    }

    def somePortTupleName = "somePortTupleName"
    def mockServiceInstance = Mock(ServiceInstance_Wrapper)
    def mockLeftPort = Mock(VirtualMachineInterface_Wrapper)
    def mockRightPort = Mock(VirtualMachineInterface_Wrapper)
    def mockManagementPort = Mock(VirtualMachineInterface_Wrapper)
    def mockPortProperties = Mock(VirtualMachineInterfacePropertiesType_Wrapper)
    def mockUtil = new MockUtil()

    def "Adding a port tuple to a service instance"() {
        given: "A correct set of attributes"

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)

        DetachedMocksKt.apiConnectorMock.create() >> Status.success()
        DetachedMocksKt.apiConnectorMock.read() >> Status.success()
        DetachedMocksKt.apiConnectorMock.update() >> Status.success()
        DetachedMocksKt.apiConnectorMock.delete() >> Status.success()
        mockLeftPort.getProperties() >> mockPortProperties
        mockRightPort.getProperties() >> mockPortProperties
        mockManagementPort.getProperties() >> mockPortProperties
        mockServiceInstance.internalId >> Sid.empty().with("ServiceInstance", "1234567890")

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addFlatIpamTonetwork,
            somePortTupleName,
            mockServiceInstance,
            mockLeftPort,
            mockRightPort,
            mockManagementPort,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        then: "Non-null ports are updated with a new port tuple"
        1 * mockLeftPort.update()
        1 * mockRightPort.update()
        1 * mockManagementPort.update()
        1 * mockLeftPort.addPortTuple(_)
        1 * mockRightPort.addPortTuple(_)
        1 * mockManagementPort.addPortTuple(_)
    }
}