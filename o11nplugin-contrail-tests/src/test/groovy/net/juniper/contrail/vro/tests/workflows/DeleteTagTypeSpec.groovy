/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.TagType
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.deleteTagTypeWorkflowName

class DeleteTagTypeSpec extends WorkflowSpec {

    def deleteTagType = workflowFromScript(deleteTagTypeWorkflowName)

    def "Deleting tag type"() {
        given:
        def tagType = dependencies.someTagType()

        when: "Script is executed"
        invokeFunction(
            deleteTagType,
            tagType
        )

        then: "Tag type with given name is deleted"
        1 * connectorMock.delete({
            def _it = it as TagType
            _it.uuid == tagType.uuid && _it.name == tagType.name
        }) >> Status.success()
    }
}
