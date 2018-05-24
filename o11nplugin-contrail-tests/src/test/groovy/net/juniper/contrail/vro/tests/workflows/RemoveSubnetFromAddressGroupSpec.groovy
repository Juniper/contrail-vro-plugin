package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.AddressGroup

class RemoveSubnetFromAddressGroupSpec extends WorkflowSpec {

    def addSubnetToAddressGroup = workflowFromScript("Add subnet to address group")
    def removeSubnetFromAddressGroup = workflowFromScript("Remove subnet from address group")

    def somePrefix = "1.2.3.4"
    def somePrefixLen = 16
    def someSubnet = "$somePrefix/$somePrefixLen".toString()

    def "Removing subnet from address group"() {
        given: "address group with a single subnet"
        def addressGroup = dependencies.someAddressGroup()
        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()
        // add a subnet to address group
        invokeFunction(
            addSubnetToAddressGroup,
            addressGroup,
            someSubnet
        )

        when: "workflow script is executed"
        invokeFunction(
            removeSubnetFromAddressGroup,
            addressGroup,
            someSubnet
        )

        then: "address group should be updated without the subnet"
        1 * connectorMock.update({
            def _it = it as AddressGroup
            _it.uuid == addressGroup.uuid &&
            !_it.prefix.subnet.any{
                it.ipPrefix == somePrefix && it.ipPrefixLen == somePrefixLen
            }
        }) >> Status.success()
    }
}
