/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.ServiceHealthCheck
import net.juniper.contrail.api.types.ServiceInstance

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddServiceInstanceToHealthCheckSpec extends WorkflowSpec {

    def addServiceInstanceToHealthCheck = workflowFromScript(addRelationWorkflowName(ServiceHealthCheck, ServiceInstance))

    def someInterfaceName = "left"

    def "Adding service instance to health check"() {
        given: "A correct set of attributes"
        def healthCheck = dependencies.someServiceHealthCheck()
        def serviceInstance = dependencies.someServiceInstance()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addServiceInstanceToHealthCheck,
            healthCheck,
            serviceInstance,
            someInterfaceName
        )

        then: "The parent health check should be updated"
        1 * connectorMock.update({
            def _it = it as ServiceHealthCheck
            _it.uuid == healthCheck.uuid &&
            _it.serviceInstance.any {
                it.to == serviceInstance.qualifiedName
            }}) >> Status.success()
    }
}
