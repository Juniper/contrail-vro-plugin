/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.Tag
import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.createWorkflowName

class CreateTagInProjectSpec extends WorkflowSpec {

    def createTagInProject = workflowFromScript(createWorkflowName(Project, Tag))
    def tagValue = "my-project-scoped-value"
    def tagType = "my-project-scoped-tag"

    def "Creating tag in project"() {
        given:
        def project = dependencies.someProject()

        connectorMock.read(_) >> Status.success()

        when: "Script is executed"
        invokeFunction(
            createTagInProject,
            project,
            tagType,
            tagValue
        )

        then: "Tag with given type and value is created"
        1 * connectorMock.create({
            def _it = it as Tag
            _it.value == tagValue && _it.typeName == tagType &&
            _it.parentUuid == project.uuid
        }) >> Status.success()
    }
}
