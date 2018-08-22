package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.AddressGroup

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addLabelToAddressGroupWorkflowName


class AddLabelToAddressGroupSpec extends WorkflowSpec {

    def addLabelToAddressGroup = workflowFromScript(addLabelToAddressGroupWorkflowName)

    def "Adding label to address group"() {
        given: "A correct set of attributes"
        def addressGroup = dependencies.someAddressGroup()
        def label = dependencies.someProjectTag()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addLabelToAddressGroup,
            addressGroup,
            label
        )

        then: "The address group is updated with the label"
        1 * connectorMock.update({
            def _it = it as AddressGroup
            _it.uuid == addressGroup.uuid &&
            _it.tag.any{
                it.to == label.qualifiedName
            }}) >> Status.success()
    }
}