/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.Subnet

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddSubnetToAddressGroupSpec extends WorkflowSpec {

    def createSubnet = workflowFromScript(addRelationWorkflowName(AddressGroup, Subnet))

    def somePrefix = "1.2.3.4"
    def somePrefixLen = 16
    def someSubnet = "$somePrefix/$somePrefixLen".toString()

    def "Adding a subnet to address group"() {
        given: "empty address group"
        def addressGroup = dependencies.someAddressGroup()

        when: "workflow script is executed"
        invokeFunction(
            createSubnet,
            addressGroup,
            someSubnet
        )

        then: "a subnet is added to the address group"
        1 * connectorMock.update({
            def _it = it as AddressGroup
            _it.uuid == addressGroup.uuid &&
            _it.prefix.subnet.size() == 1 &&
            _it.prefix.subnet.any {
                it.ipPrefix == somePrefix  && it.ipPrefixLen == somePrefixLen
            }
        }) >> Status.success()
    }
}
