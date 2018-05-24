/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FloatingIp

class RemoveFloatingIpFromPortSpec extends WorkflowSpec {

    def addFloatingIpToPort = workflowFromScript("Add floating IP to port")
    def removeFloatingIpFromPort = workflowFromScript("Remove floating IP from port")

    def "Removing a floating IP from a port"() {
        given: "A correct set of attributes"
        def port = dependencies.somePort()
        def floatingIp = dependencies.someFloatingIp()

        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()
        // add a floating IP to the port
        invokeFunction(
            addFloatingIpToPort,
            port,
            floatingIp
        )

        when: "Running the script"
        invokeFunction(
            removeFloatingIpFromPort,
            port,
            floatingIp
        )

        then: "The floating IP is updated without the new port"
        1 * connectorMock.update({
            def _it = it as FloatingIp
            _it.uuid == floatingIp.uuid &&
            !_it.virtualMachineInterface.any{
                it.to == port.qualifiedName
            }}) >> Status.success()
    }
}