/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class SomeSpec extends WorkflowSpec {

    def addFlatIpamToNetwork = getWorkflowScript("Add network IPAM to virtual network")

    def "Adding "() {
        given: "A correct set of attributes"
        def parent = dependencies.someVirtualNetwork()
        def item = dependencies.someNetworkIpam()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addFlatIpamToNetwork,
            parent,
            item
        )

        then: "The "
        1 * connectorMock.update({
            it.uuid == parent.uuid}) >> Status.success()
    }
}