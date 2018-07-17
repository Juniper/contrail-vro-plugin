/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkPolicy
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.addRuleToNetworkPolicyWorkflowName
import static net.juniper.contrail.vro.config.constants.WorkflowNamesKt.removeRuleFromNetworkPolicyWorkflowName

class RemoveRuleFromPolicySpec extends WorkflowSpec {

    def addPolicyRule = workflowFromScript(addRuleToNetworkPolicyWorkflowName)
    def removeRuleFromPolicy = workflowFromScript(removeRuleFromNetworkPolicyWorkflowName)

    def someSimpleAction = "pass"
    def someProtocol = "tcp"
    def someDirection = "<>"
    def someAddressType = "Network"
    def someNetworkType = "any"
    def someNetwork = null
    def someCIDR = null
    def someSecurityGroup = null
    def someNetworkPolicy= null
    def somePorts = "any"
    def someLog = false
    def someDefineServices = false
    def someDefineMirror = false
    def someServiceInstances = null
    def someMirrorType = null
    def someAnalyzerInstance = null
    def someAnalyzerName = null
    def someNicAssistedVlan = null
    def someAnalyzerIP = null
    def someAnalyzerMac = null
    def someUdpPort = null
    def someJuniperHeader = null
    def someRoutingInstance = null
    def someNexthopMode = null
    def someVtepDestIP = null
    def someVtepDestMac = null
    def someVni = null

    def "Removing a policy rule"() {
        given: "A correct set of attributes"
        def networkPolicy = dependencies.someNetworkPolicy()

        def initialSize = networkPolicy.getEntries()?.getPolicyRule()?.size() ?: 0
        connectorMock.read(_) >> Status.success()
        connectorMock.update(_) >> Status.success()
        // add a rule to the network policy
        invokeFunction(
            addPolicyRule,
            networkPolicy,
            someSimpleAction,
            someProtocol,
            someDirection,
            someAddressType,
            someCIDR,
            someNetworkType,
            someNetwork,
            someNetworkPolicy,
            someSecurityGroup,
            somePorts,
            someAddressType,
            someCIDR,
            someNetworkType,
            someNetwork,
            someNetworkPolicy,
            someSecurityGroup,
            somePorts,
            someLog,
            someDefineServices,
            someDefineMirror,
            someServiceInstances,
            someMirrorType,
            someAnalyzerInstance,
            someAnalyzerName,
            someNicAssistedVlan,
            someAnalyzerIP,
            someAnalyzerMac,
            someUdpPort,
            someJuniperHeader,
            someRoutingInstance,
            someNexthopMode,
            someVtepDestIP,
            someVtepDestMac,
            someVni
        )
        def lastRule = "$initialSize: someRuleParams"

        when: "Running the script"
        invokeFunction(
            removeRuleFromPolicy,
            networkPolicy,
            lastRule
        )

        then: "The policy is updated without the new rule"
        1 * connectorMock.update({
            def _it = it as NetworkPolicy
            _it.uuid == networkPolicy.uuid &&
            _it.entries.policyRule.size() == initialSize &&
            !_it.entries.policyRule.any{
                it.actionList.simpleAction == someSimpleAction &&
                it.protocol == someProtocol
            }}) >> Status.success()
    }
}