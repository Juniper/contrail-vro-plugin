package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.gen.ServiceInstance_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterface_Wrapper
import org.spockframework.mock.MockUtil

class AddPortTupleToServiceInstanceSpec extends WorkflowSpec {

    def addPortTupleToServiceInstance = engine.getFunctionFromWorkflowScript(workflows, "Add port tuple to service instance")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailVirtualMachineInterfacePropertiesType = Java.type('net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper');")
        engine.engine.eval("var ContrailPortTuple = Java.type('net.juniper.contrail.vro.gen.PortTuple_Wrapper');")
    }

    def somePortTupleName = "somePortTupleName"
    def mockUtil = new MockUtil()

    def "Adding a port tuple to a service instance"() {
        given: "A correct set of attributes"
        def dependencies = createContextAndDependencies()
        def project1 = dependencies.someProject()
        def serviceInstance = dependencies.someServiceInstance(project1)
        def leftPort = dependencies.somePort(project1)
        def rightPort = dependencies.somePort(project1)
        def managementPort = dependencies.somePort(project1)
        def portProperties = dependencies.somePortProperties()
        leftPort.setProperties(portProperties)
        rightPort.setProperties(portProperties)
        managementPort.setProperties(portProperties)

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)

        DetachedMocksKt.apiConnectorMock.create(_) >> Status.success()
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()
        DetachedMocksKt.apiConnectorMock.delete(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()

        println(leftPort.getPortTuple())

        engine.invokeFunction(
            addPortTupleToServiceInstance,
            somePortTupleName,
            serviceInstance,
            leftPort,
            rightPort,
            managementPort,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        println(leftPort.getPortTuple())

        then: "Non-null ports are updated with a new port tuple"
        1 * DetachedMocksKt.apiConnectorMock.update({it.uuid == leftPort.uuid}) >> Status.success()
        1 * DetachedMocksKt.apiConnectorMock.update({it.uuid == rightPort.uuid}) >> Status.success()
        1 * DetachedMocksKt.apiConnectorMock.update({it.uuid == managementPort.uuid}) >> Status.success()
    }
}