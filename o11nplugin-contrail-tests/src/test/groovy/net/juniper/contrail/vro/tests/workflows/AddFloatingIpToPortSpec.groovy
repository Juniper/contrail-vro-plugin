/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status

class AddFloatingIpToPortSpec extends WorkflowSpec {

    def addFloatingIpToPort = getWorkflowScript("Add floating IP to port")

    def someIsFixedIpAddress = true

    def "Adding a floating IP to a port"() {
        given: "A correct set of attributes"
        def port = dependencies.somePort()
        def floatingIp = dependencies.someFloatingIp()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addFloatingIpToPort,
            port,
            floatingIp,
            someIsFixedIpAddress
        )

        then: "The floating IP object is updated"
        1 * connectorMock.update({
            it.uuid == floatingIp.uuid &&
            it.virtualMachineInterface.any {
                it.to == port.qualifiedName
            }}) >> Status.success()
    }
}
