/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FloatingIp

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.createSimpleWorkflowName

class CreateFloatingIpSpec extends WorkflowSpec {

    def createFloatingIp = workflowFromScript(createSimpleWorkflowName(FloatingIp))

    def "Creating floating IP"() {
        given: "A correct set of attributes"
        def parent = dependencies.someFloatingIpPool()
        def someProjects = null
        def someAddress = null

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            createFloatingIp,
            parent,
            someProjects,
            someAddress
        )

        then: "A floating IP with given parameters is created"
        1 * connectorMock.create({it.parentUuid == parent.uuid}) >> Status.success()
    }
}
