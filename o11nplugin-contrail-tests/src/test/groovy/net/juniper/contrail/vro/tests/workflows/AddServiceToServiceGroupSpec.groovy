/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.ServiceGroup

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addServiceToServiceGroupWorkflowName

class AddServiceToServiceGroupSpec extends WorkflowSpec {

    def workflowScript = workflowFromScript(addServiceToServiceGroupWorkflowName)

    def someProtocol = "tcp"
    def startPort = 157
    def endPort = 2391
    def somePorts = "$startPort-$endPort".toString()

    def "Adding service to a service group"() {
        given:
        def serviceGroup = dependencies.someGlobalServiceGroup()

        def initialSize = serviceGroup.getFirewallServiceList()?.getFirewallService()?.size() ?: 0
        connectorMock.read(_) >> Status.success()

        when: "workflow is executed"
        invokeFunction(
            workflowScript,
            serviceGroup,
            someProtocol,
            somePorts
        )

        then: "the parent service group should be updated."
        1 * connectorMock.update({
            def _it = it as ServiceGroup
            _it.uuid == serviceGroup.uuid &&
            _it.firewallServiceList.firewallService.size() == initialSize + 1 &&
            _it.firewallServiceList.firewallService.any{
                it.protocol == someProtocol &&
                it.dstPorts.startPort == startPort &&
                it.dstPorts.endPort == endPort
            }}) >> Status.success()
    }
}
