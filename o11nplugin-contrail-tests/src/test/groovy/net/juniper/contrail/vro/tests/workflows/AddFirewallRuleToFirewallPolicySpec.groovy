package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FirewallPolicy

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddFirewallRuleToFirewallPolicySpec extends WorkflowSpec {

    def addFirewallRuleToFirewallPolicy = workflowFromScript(addRelationWorkflowName(FirewallPolicy, FirewallRule))

    def "Adding a firewall rule to a firewall policy"() {
        given: "A correct set of attributes"
        def firewallPolicy = dependencies.someProjectFirewallPolicy()
        def firewallRule = dependencies.someProjectFirewallRule()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addFirewallRuleToFirewallPolicy,
            firewallPolicy,
            firewallRule
        )

        then: "The firewall policy object is updated with the firewall rule"
        1 * connectorMock.update({
            def _it = it as FirewallPolicy
            _it.uuid == firewallPolicy.uuid &&
            _it.firewallRule.any{
                it.to == firewallRule.qualifiedName
            }}) >> Status.success()
    }
}
