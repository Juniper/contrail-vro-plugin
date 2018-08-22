package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.Tag

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName
import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.removeRelationWorkflowName

class RemoveTagFromAPSSpec extends WorkflowSpec {

    def addTagToAPS = workflowFromScript(addRelationWorkflowName(ApplicationPolicySet, Tag))
    def removeTagFromAPS = workflowFromScript(removeRelationWorkflowName(ApplicationPolicySet, Tag))

    def "Removing Tag from Application Policy Set"() {

        given: "A correct set of attributes"
        def globalTag = dependencies.someGlobalTag()
        def aps = dependencies.someGlobalApplicationPolicySet()

        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()

        invokeFunction(
            addTagToAPS,
            aps,
            globalTag
        )

        when: "Running the script"
        invokeFunction(
            removeTagFromAPS,
            aps,
            globalTag
        )

        then: "The application policy set is updated without the tag"
        1 * connectorMock.update({
            def _it = it as ApplicationPolicySet
            _it.uuid == aps.uuid &&
            !_it.tag.any{
                it.to == globalTag.qualifiedName
            }}) >> Status.success()
    }
}