package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FirewallRule

import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.editFirewallRuleWorkflowName

class EditFirewallRuleSpec extends WorkflowSpec {

    def editProjectFirewallRule = workflowFromScript(editFirewallRuleWorkflowName)

    def "Editing firewall rule"() {
        given: "A correct set of attributes"

        //someProjectFirewallRule() creates a FirewallRule with all parameters as null (except uuid and name)
        def firewallRule = dependencies.someProjectFirewallRule()
        def ruleUuid = firewallRule.uuid

        def action = "pass"
        def direction = "<"
        def serviceType = "manual"
        def serviceProtocol = "any"
        def serviceSrcPorts = "any"
        def serviceDSTPorts = "any"
        def matchTags = ["application"]

        connectorMock.read(_) >> Status.success()
        connectorMock.create(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            editProjectFirewallRule,
            firewallRule,
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

        then: "the firewall rule has the given parameters"
        1 * connectorMock.update({
            def _it = it as FirewallRule
            _it.uuid == ruleUuid &&
            _it.actionList.simpleAction == action &&
            _it.direction == direction &&
            _it.matchTags.tagList == matchTags
        }) >> Status.success()
    }
}
