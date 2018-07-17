/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.VirtualMachineInterface

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddFloatingIpToPortSpec extends WorkflowSpec {

    def addFloatingIpToPort = workflowFromScript(addRelationWorkflowName(VirtualMachineInterface, FloatingIp))

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
            def _it = it as FloatingIp
            _it.uuid == floatingIp.uuid &&
            _it.virtualMachineInterface.any {
                it.to == port.qualifiedName
            }}) >> Status.success()
    }
}
