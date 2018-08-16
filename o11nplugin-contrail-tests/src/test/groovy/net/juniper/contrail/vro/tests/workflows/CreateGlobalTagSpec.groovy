/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.Tag
import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.createGlobalWorkflowName

class CreateGlobalTagSpec extends WorkflowSpec {

    def createGlobalTag = workflowFromScript(createGlobalWorkflowName(Tag))
    def tagValue = "my-global-value"
    def tagType = "my-global-tag"

    def "Creating global tag"() {
        given:
        def connection = dependencies.connection

        connectorMock.read(_) >> Status.success()

        when: "Script is executed"
        invokeFunction(
            createGlobalTag,
            connection,
            tagType,
            tagValue
        )

        then: "Tag with given type and value is created"
        1 * connectorMock.create({
            def _it = it as Tag
            _it.value == tagValue && _it.typeName == tagType
        }) >> Status.success()
    }
}
