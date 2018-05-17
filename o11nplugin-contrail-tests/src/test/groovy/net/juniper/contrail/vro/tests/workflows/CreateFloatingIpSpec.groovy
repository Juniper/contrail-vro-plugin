/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class CreateFloatingIpSpec extends WorkflowSpec {

    def createFloatingIp = getWorkflowScript("Create floating IP")

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
