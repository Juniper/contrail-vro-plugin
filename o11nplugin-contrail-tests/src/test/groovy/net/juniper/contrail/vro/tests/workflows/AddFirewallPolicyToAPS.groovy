package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.ApplicationPolicySet

import static net.juniper.contrail.vro.workflows.util.DslUtilsKt.addRelationWorkflowName

class AddFirewallPolicyToAPS extends WorkflowSpec {

    def addFirewallPolicyToAPS = workflowFromScript(addRelationWorkflowName(ApplicationPolicySet, FirewallPolicy))

    def "Adding a firewall policy to an application policy set"() {
        given: "A correct set of attributes"
        def firewallPolicy = dependencies.someGlobalFirewallPolicy()
        def aps = dependencies.someGlobalApplicationPolicySet()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addFirewallPolicyToAPS,
            aps,
            firewallPolicy
        )

        then: "The application policy set object is updated with the firewall policy"
        1 * connectorMock.update({
            def _it = it as ApplicationPolicySet
            _it.uuid == aps.uuid &&
            _it.firewallPolicy.any{
                it.to == firewallPolicy.qualifiedName
            }}) >> Status.success()
    }
}