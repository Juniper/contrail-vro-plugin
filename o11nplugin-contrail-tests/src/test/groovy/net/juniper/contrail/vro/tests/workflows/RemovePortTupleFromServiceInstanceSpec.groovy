/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.gen.PortTuple_Wrapper
import org.spockframework.mock.MockUtil

class RemovePortTupleFromServiceInstanceSpec extends WorkflowSpec {

    def addPortTupleToServiceInstance = getWorkflowScript("Add port tuple to service instance")
    def removePortTupleFromServiceInstance = getWorkflowScript("Remove port tuple from service instance")

    def somePortTupleName = "somePortTupleName"

    PortTuple tuple = null

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
//        def createdTupleWrapper = new PortTuple_Wrapper()
//        def createdTuple = new PortTuple()
//        createdTupleWrapper.__setTarget(createdTuple)
//        createdTuple.setParent(serviceInstance.__getTarget())
//        createdTuple.setName(somePortTupleName)
//        leftPort.addPortTuple(createdTupleWrapper)
//        if (createdTuple.virtualMachineInterfaceBackRefs == null) {
//            println('wat')
//        }
//        createdTuple.virtualMachineInterfaceBackRefs.add(new ObjectReference<ApiPropertyBase>(leftPort.qualifiedName, null))
//        createdTuple.virtualMachineInterfaceBackRefs.add(new ObjectReference<ApiPropertyBase>(rightPort.qualifiedName, null))
//        createdTuple.virtualMachineInterfaceBackRefs.add(new ObjectReference<ApiPropertyBase>(managementPort.qualifiedName, null))

        connectorMock.create(_) >> Status.success()
        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()
//        connectorMock.getObjects(PortTuple.class, _) >> [createdTuple]
//        connectorMock.getObjects(VirtualMachineInterface.class, _) >> [leftPort, rightPort, managementPort]

        when: "Running the scripts"

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

//        def tuple_W = new PortTuple_Wrapper()
//        tuple_W.__setTarget(tuple)
//        tuple_W.setParentServiceInstance(serviceInstance)
//        tuple_W.setName(somePortTupleName)
        def tuple = dependencies.somePortTuple(serviceInstance)
        tuple.setName(somePortTupleName)
        invokeFunction(
            removePortTupleFromServiceInstance,
            serviceInstance,
            tuple
        )

        then: "Non-null ports are updated with a new port tuple"
//        1 * connectorMock.update({
//            def _it = it as VirtualMachineInterface
//            _it.uuid == leftPort.uuid &&
//            _it.portTuple.any{
//                it.to == serviceInstance.qualifiedName + somePortTupleName
//            }}) >> Status.success()
//        1 * connectorMock.update({
//            def _it = it as VirtualMachineInterface
//            _it.uuid == rightPort.uuid &&
//            _it.portTuple.any{
//                it.to == serviceInstance.qualifiedName + somePortTupleName
//            }}) >> Status.success()
//        1 * connectorMock.update({
//            def _it = it as VirtualMachineInterface
//            _it.uuid == managementPort.uuid &&
//            _it.portTuple.any{
//                it.to == serviceInstance.qualifiedName + somePortTupleName
//            }}) >> Status.success()
//
//        and: "The port tuple is created"
//        1 * connectorMock.create({
//            def _it = it as PortTuple
//            _it.name == somePortTupleName
//        }) >> {
//            arguments -> tuple = arguments[0] as PortTuple
//            return Status.success()
//        }
//
//        and: "The port tuple is removed"
//        1 * connectorMock.update({
//            def _it = it as VirtualMachineInterface
//            _it.uuid == leftPort.uuid &&
//            !_it.portTuple.any{
//                it.to == serviceInstance.qualifiedName + somePortTupleName
//            }}) >> Status.success()
//        1 * connectorMock.update({
//            def _it = it as VirtualMachineInterface
//            _it.uuid == rightPort.uuid &&
//            !_it.portTuple.any{
//                it.to == serviceInstance.qualifiedName + somePortTupleName
//            }}) >> Status.success()
//        1 * connectorMock.update({
//            def _it = it as VirtualMachineInterface
//            _it.uuid == managementPort.uuid &&
//            !_it.portTuple.any{
//                it.to == serviceInstance.qualifiedName + somePortTupleName
//            }}) >> Status.success()

        and: "The port tuple is deleted"
        1 * connectorMock.delete({
            def _it = it as PortTuple
            _it.name == somePortTupleName
        }) >> Status.success()
    }
}