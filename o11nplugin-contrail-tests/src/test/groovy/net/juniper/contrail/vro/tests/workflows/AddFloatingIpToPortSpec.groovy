/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class AddFloatingIpToPortSpec extends WorkflowSpec {

    def addFloatingIpToPort = engine.getFunctionFromWorkflowScript(workflows, "Add floating IP to port")
    def loadWrapperTypes = {
    }

    def mockUtil = new MockUtil()

    def someIsFixedIpAddress = true

    def "Adding a floating IP to a port"() {
        given: "A correct set of attributes"
        def dependencies = createContextAndDependencies()
        def port = dependencies.somePort()
        def floatingIp = dependencies.someFloatingIp()

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()
        engine.invokeFunction(
            addFloatingIpToPort,
            port,
            floatingIp,
            someIsFixedIpAddress
        )

        then: "The floating IP object is updated"
        1 * DetachedMocksKt.apiConnectorMock.update({
            it.uuid == floatingIp.uuid &&
            it.virtualMachineInterface.any {
                it.to == port.qualifiedName
            }}) >> Status.success()
    }
}
