/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.ServiceGroup

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addServiceToServiceGroupWorkflowName
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.editServiceOfServiceGroupWorkflowName

class EditServiceOfServiceGroupSpec extends WorkflowSpec {

    def workflowScript = workflowFromScript(editServiceOfServiceGroupWorkflowName)
    def addServiceScript = workflowFromScript(addServiceToServiceGroupWorkflowName)

    def someProtocol = "tcp"
    def startPort = 157
    def endPort = 2391
    def somePorts = "$startPort-$endPort".toString()

    def someOtherProtocol = "udp"
    def someOtherStartPort = 73
    def someOtherEndPort = 372
    def someOtherPorts = "$someOtherStartPort-$someOtherEndPort".toString()

    def "Editing service of service group"() {
        given:
        def serviceGroup = dependencies.someGlobalServiceGroup()

        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()
        def initialSize = serviceGroup.getFirewallServiceList()?.getFirewallService()?.size() ?: 0
        def serviceToEdit = "$initialSize: dummy representation"
        //add service to service group
        invokeFunction(
            addServiceScript,
            serviceGroup,
            someProtocol,
            somePorts
        )
        def sizeBeforeEdit = serviceGroup.getFirewallServiceList().getFirewallService().size()

        when: "workflow is executed"
        invokeFunction(
            workflowScript,
            serviceGroup,
            serviceToEdit,
            someOtherProtocol,
            someOtherPorts
        )

        then: "the parent service group should be updated."
        1 * connectorMock.update({
            def _it = it as ServiceGroup
            _it.uuid == serviceGroup.uuid &&
            _it.firewallServiceList.firewallService.size() == sizeBeforeEdit &&
            ! _it.firewallServiceList.firewallService.any{
                it.protocol == someProtocol &&
                it.dstPorts.startPort == startPort &&
                it.dstPorts.endPort == endPort
            } &&
            _it.firewallServiceList.firewallService.any{
                it.protocol == someOtherProtocol &&
                it.dstPorts.startPort == someOtherStartPort &&
                it.dstPorts.endPort == someOtherEndPort
            }
        }) >> Status.success()
    }
}
