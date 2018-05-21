/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkPolicy
import org.spockframework.mock.MockUtil

class EditPolicyRuleSpec extends WorkflowSpec {

    def addPolicyRule = getWorkflowScript("Add rule to network policy")
    def editPolicyRule = getWorkflowScript("Edit rule of network policy")

    def someSimpleAction = "pass"
    def someDifferentSimpleAction = "deny"
    def someProtocol = "tcp"
    def someDifferentProtocol = "udp"
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
    def firstRule = "0: someRuleParams"

    def "Editing a policy rule"() {
        given: "A correct set of attributes"
        def networkPolicy = dependencies.someNetworkPolicy()

        def initialSize = networkPolicy.getEntries()?.getPolicyRule()?.size() ?: 0
        connectorMock.read(_) >> Status.success()

        when: "Running the scripts"
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
        // edit the added rule
        invokeFunction(
            editPolicyRule,
            networkPolicy,
            firstRule,
            someDifferentSimpleAction,
            someDifferentProtocol,
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

        then: "The rule is correctly added to the policy"
        1 * connectorMock.update({
            def _it = it as NetworkPolicy
            _it.uuid == networkPolicy.uuid &&
            _it.entries.policyRule.size() == initialSize + 1 &&
            _it.entries.policyRule.any{
                it.actionList.simpleAction == someSimpleAction &&
                it.protocol == someProtocol
            }}) >> Status.success()
        then: "The policy is updated with the modified rule"
        1 * connectorMock.update({
            def _it = it as NetworkPolicy
            _it.uuid == networkPolicy.uuid &&
            _it.entries.policyRule.size() == initialSize + 1 &&
            _it.entries.policyRule.any{
                it.actionList.simpleAction == someDifferentSimpleAction &&
                it.protocol == someDifferentProtocol
            }}) >> Status.success()
    }
}