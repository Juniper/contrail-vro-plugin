package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.AddressGroup

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.removeLabelFromAddressGroupWorkflowName
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addLabelToAddressGroupWorkflowName


class RemoveLabelFromAddressGroupSpec extends WorkflowSpec {

    def removeLabelFromAddressGroup = workflowFromScript(removeLabelFromAddressGroupWorkflowName)
    def addLabelToAddressGroup = workflowFromScript(addLabelToAddressGroupWorkflowName)

    def "Removing label from address group"() {
        given: "A correct set of attributes"
        def addressGroup = dependencies.someAddressGroup()
        def label = dependencies.someProjectTag()

        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()

        // add a Label to the AddressGroup
        invokeFunction(
            addLabelToAddressGroup,
            addressGroup,
            label
        )

        when: "Running the script"
        invokeFunction(
            removeLabelFromAddressGroup,
            addressGroup,
            label
        )

        then: "The address group is updated without the label"
        1 * connectorMock.update({
            def _it = it as AddressGroup
            _it.uuid == addressGroup.uuid &&
            !_it.tag.any{
                it.to == label.qualifiedName
            }}) >> Status.success()
    }
}