/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.tests.TestUtilsKt

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName
import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.removeRelationWorkflowName

class RemovePortTupleFromServiceInstanceSpec extends WorkflowSpec {

    def addPortTupleToServiceInstance = workflowFromScript(addRelationWorkflowName(ServiceInstance, PortTuple))
    def removePortTupleFromServiceInstance = workflowFromScript(removeRelationWorkflowName(ServiceInstance, PortTuple))

    def somePortTupleName = "somePortTupleName"

    def "Removing a port tuple from a service instance"() {
        given: "A correct set of attributes"
        def project1 = dependencies.someProject()
        def serviceInstance = dependencies.someServiceInstance(project1)
        def leftPort = dependencies.somePort(project1)
        def rightPort = dependencies.somePort(project1)
        def managementPort = dependencies.somePort(project1)
        def portProperties = dependencies.somePortProperties()
        leftPort.setProperties(portProperties)
        rightPort.setProperties(portProperties)
        managementPort.setProperties(portProperties)
        def expectedPortTupleQualifiedName = serviceInstance.qualifiedName + somePortTupleName

        PortTuple tuple = null
        // we need to intercept the created port tuple to delete it
        connectorMock.create(_) >> {
            arguments -> tuple = arguments[0] as PortTuple
            return Status.success()
        }
        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()

        invokeFunction(
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
        // setting the reference list to emtpy list instead of null
        // allows us to get the object list from the connector mock
        TestUtilsKt.setField(serviceInstance.__getTarget(), "port_tuples", [])
        TestUtilsKt.setField(tuple, "virtual_machine_interface_back_refs", [])
        connectorMock.getObjects(PortTuple.class, _) >> [tuple]
        connectorMock.getObjects(VirtualMachineInterface.class, _) >> [leftPort.__getTarget(), rightPort.__getTarget(), managementPort.__getTarget()]

        def wrappedTuple = serviceInstance.portTuples[0]

        when: "Running the script"
        invokeFunction(
            removePortTupleFromServiceInstance,
            serviceInstance,
            wrappedTuple
        )

        then: "The port tuple is removed"
        1 * connectorMock.update({
            def _it = it as VirtualMachineInterface
            _it.uuid == leftPort.uuid &&
            !_it.portTuple.any{
                it.to == expectedPortTupleQualifiedName
            }}) >> Status.success()
        1 * connectorMock.update({
            def _it = it as VirtualMachineInterface
            _it.uuid == rightPort.uuid &&
            !_it.portTuple.any{
                it.to == expectedPortTupleQualifiedName
            }}) >> Status.success()
        1 * connectorMock.update({
            def _it = it as VirtualMachineInterface
            _it.uuid == managementPort.uuid &&
            !_it.portTuple.any{
                it.to == expectedPortTupleQualifiedName
            }}) >> Status.success()

        and: "The port tuple is deleted"
        1 * connectorMock.delete({
            def _it = it as PortTuple
            _it.name == somePortTupleName
        }) >> Status.success()
    }
}