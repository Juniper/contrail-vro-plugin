/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

class CreateServiceTemplateSpec extends WorkflowSpec {

    def createServiceTemplate = workflowFromScript("Create service template")

    def someName = "someName"
    def someVersion = 2
    def someServiceMode = "transparent"
    def someServiceType = "firewall"
    def someServiceVirtualizationType = null
    def someInterfaceType = ["left", "right", "management"]

    def "Creating service template"() {
        given: "A correct set of attributes"
        def connection = dependencies.connection

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            createServiceTemplate,
            connection,
            someName,
            someVersion,
            someServiceMode,
            someServiceType,
            someServiceVirtualizationType,
            someInterfaceType
        )

        then: "The service template is created with correct name"
        1 * connectorMock.create({it.name == someName}) >> Status.success()
    }
}