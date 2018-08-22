/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.PolicyManagement
import net.juniper.contrail.vro.gen.PolicyManagement_Wrapper

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.createGlobalWorkflowName

class CreateGlobalFirewallRuleSpec extends WorkflowSpec {

    def createGlobalFirewallRule = workflowFromScript(createGlobalWorkflowName(FirewallRule))

    def "Creating global firewall rule"() {
        given: "A correct set of attributes"
        def connection = dependencies.connection
        def defaultPolicyManagement = dependencies.defaultPolicyManagement
        def action = "pass"
        def direction = "<"
        def serviceType = "manual"
        def serviceProtocol = "any"
        def serviceSrcPorts = "any"
        def serviceDSTPorts = "any"
        def matchTags = ["application"]

        connectorMock.read(_) >> Status.success()
        connectorMock.findByFQN(PolicyManagement.class, defaultPolicyManagement.qualifiedName.join(":")) >> dependencies.defaultPolicyManagement.__getTarget()

        when: "Running the script"
        invokeFunction(
            createGlobalFirewallRule,
            connection,
            "none",
            null,
            null,
            null,
            "none",
            null,
            null,
            null,
            action,
            direction,
            serviceType,
            serviceProtocol,
            serviceSrcPorts,
            serviceDSTPorts,
            null,
            matchTags,
        )

        then: "A global firewall rule with given parameters is created"
        1 * connectorMock.create({
            def _it = it as FirewallRule
            _it.parent.qualifiedName == dependencies.defaultPolicyManagement.qualifiedName &&
            _it.actionList.simpleAction == action &&
            _it.direction == direction &&
            _it.matchTags.tagList == matchTags
        }) >> Status.success()
    }
}