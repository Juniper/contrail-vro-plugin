/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.TagType

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.createRelationWorkflowName

class CreateTagTypeSpec extends WorkflowSpec {

    def createTagType = workflowFromScript(createRelationWorkflowName(TagType))
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
