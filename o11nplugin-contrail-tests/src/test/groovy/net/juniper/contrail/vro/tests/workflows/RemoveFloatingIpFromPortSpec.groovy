/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.VirtualNetwork
import org.spockframework.mock.MockUtil

class RemoveFloatingIpFromPortSpec extends WorkflowSpec {

    def addFloatingIpToPort = getWorkflowScript("Add floating IP to port")
    def removeFloatingIpFromPort = getWorkflowScript("Remove floating IP from port")

    def "Removing a floating IP from a port"() {
        given: "A correct set of attributes"
        def port = dependencies.somePort()
        def floatingIp = dependencies.someFloatingIp()

        connectorMock.read(_) >> Status.success()

        when: "Running the scripts"
        invokeFunction(
            addFloatingIpToPort,
            port,
            floatingIp
        )
        invokeFunction(
            removeFloatingIpFromPort,
            port,
            floatingIp
        )

        then: "A floating IP is added to the port"
        1 * connectorMock.update({
            def _it = it as FloatingIp
            _it.uuid == floatingIp.uuid &&
            _it.virtualMachineInterface.any{
                it.to == port.qualifiedName
            }}) >> Status.success()
        then: "The floating IP is updated without the new port"
        1 * connectorMock.update({
            def _it = it as FloatingIp
            _it.uuid == floatingIp.uuid &&
            !_it.virtualMachineInterface.any{
                it.to == port.qualifiedName
            }}) >> Status.success()
    }
}