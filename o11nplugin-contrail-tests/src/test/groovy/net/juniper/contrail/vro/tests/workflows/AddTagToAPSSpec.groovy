/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.Tag

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddTagToAPSSpec extends WorkflowSpec {

    def addTagToAPS = workflowFromScript(addRelationWorkflowName(ApplicationPolicySet, Tag))

    def "Adding a tag to an application policy set"() {
        given: "A correct set of attributes"
        def globalTag = dependencies.someGlobalTag()
        def aps = dependencies.someGlobalApplicationPolicySet()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addTagToAPS,
            aps,
            globalTag
        )

        then: "The application policy set is updated with the tag"
        1 * connectorMock.update({
            def _it = it as ApplicationPolicySet
            _it.uuid == aps.uuid &&
            _it.getTag().any{
                it.to == globalTag.qualifiedName
            }}) >> Status.success()
    }
}
