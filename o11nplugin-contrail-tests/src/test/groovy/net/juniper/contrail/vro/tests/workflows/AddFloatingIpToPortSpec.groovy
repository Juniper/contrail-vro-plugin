/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.FloatingIp_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterface_Wrapper

class AddFloatingIpToPortSpec extends WorkflowSpec {

    def addFloatingIpToPort = engine.getFunctionFromWorkflowScript(workflows, "Add floating IP to port")
    def loadWrapperTypes = {
    }

    def mockPort = Mock(VirtualMachineInterface_Wrapper)
    def mockFloatingIP = Mock(FloatingIp_Wrapper)

    def someIsFixedIpAddress = true

    def "Adding a floating IP to a port"() {
        given: "A correct set of attributes"

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addFloatingIpToPort,
            mockPort,
            mockFloatingIP,
            someIsFixedIpAddress
        )

        then: "A floating IP - Port relation is established"
        1 * mockFloatingIP.addPort(mockPort)

        and: "The floating IP object is updated"
        1 * mockFloatingIP.update()
    }
}
