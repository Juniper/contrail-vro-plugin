/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddPortTupleToServiceInstanceSpec extends WorkflowSpec {

    def addPortTupleToServiceInstance = workflowFromScript(addRelationWorkflowName(ServiceInstance, PortTuple))

    def somePortTupleName = "somePortTupleName"

    def "Adding a port tuple to a service instance"() {
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

        connectorMock.read(_) >> Status.success()
        connectorMock.delete(_) >> Status.success()

        when: "Running the script"
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

        then: "Non-null ports are updated with a new port tuple"
        1 * connectorMock.update({
            def _it = it as VirtualMachineInterface
            _it.uuid == leftPort.uuid &&
            _it.portTuple.any{
                it.to == serviceInstance.qualifiedName + somePortTupleName
            }}) >> Status.success()
        1 * connectorMock.update({
            def _it = it as VirtualMachineInterface
            _it.uuid == rightPort.uuid &&
            _it.portTuple.any{
                it.to == serviceInstance.qualifiedName + somePortTupleName
            }}) >> Status.success()
        1 * connectorMock.update({
            def _it = it as VirtualMachineInterface
            _it.uuid == managementPort.uuid &&
            _it.portTuple.any{
                it.to == serviceInstance.qualifiedName + somePortTupleName
            }}) >> Status.success()

        and: "The port tuple is created"
        1 * connectorMock.create({
            def _it = it as PortTuple
            _it.name == somePortTupleName
        }) >> Status.success()
    }
}