/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.createTagTypeWorkflowName

class CreateTagTypeSpec extends WorkflowSpec {

    def createTagType = workflowFromScript(createTagTypeWorkflowName)
    def tagName = "my-custom-tag"

    def "Creating tag type"() {
        given:
        def connection = dependencies.connection

        connectorMock.read(_) >> Status.success()

        when: "Script is executed"
        invokeFunction(
            createTagType,
            tagName,
            connection
        )

        then: "Tag type with given name is created"
        1 * connectorMock.create({it.name == tagName}) >> Status.success()
    }
}
