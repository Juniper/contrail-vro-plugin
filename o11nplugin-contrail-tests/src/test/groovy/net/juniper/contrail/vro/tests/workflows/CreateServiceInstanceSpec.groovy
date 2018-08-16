/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.ServiceInstance

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.createSimpleWorkflowName

class CreateServiceInstanceSpec extends WorkflowSpec {

    def createServiceInstance = workflowFromScript(createSimpleWorkflowName(ServiceInstance))

    def someInstanceName = "someInstanceName"
    def someVirtualRouterId = null
    def nonExistentInterface = null

    def "Create service instance"() {
        given: "A correct set of attributes"
        def project = dependencies.someProject()
        def serviceTemplate = dependencies.someServiceTemplate()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            createServiceInstance,
            project,
            someInstanceName,
            serviceTemplate,
            someVirtualRouterId,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface,
            nonExistentInterface
        )

        then: "A service instance with correct parent ID is created"
        1 * connectorMock.create({it.parentUuid == project.uuid}) >> Status.success()
    }
}