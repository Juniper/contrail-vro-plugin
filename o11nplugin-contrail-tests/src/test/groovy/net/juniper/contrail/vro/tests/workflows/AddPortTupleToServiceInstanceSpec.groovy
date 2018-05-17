/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

class AddPortTupleToServiceInstanceSpec extends WorkflowSpec {

    def addPortTupleToServiceInstance = getWorkflowScript("Add port tuple to service instance")

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

        connectorMock.create(_) >> Status.success()
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
            it.uuid == leftPort.uuid &&
                it.portTuple.any{
                    it.to == serviceInstance.qualifiedName + somePortTupleName
                }}) >> Status.success()
        1 * connectorMock.update({
            it.uuid == rightPort.uuid &&
                it.portTuple.any{
                    it.to == serviceInstance.qualifiedName + somePortTupleName
                }}) >> Status.success()
        1 * connectorMock.update({
            it.uuid == managementPort.uuid &&
                it.portTuple.any{
                    it.to == serviceInstance.qualifiedName + somePortTupleName
                }}) >> Status.success()
    }
}