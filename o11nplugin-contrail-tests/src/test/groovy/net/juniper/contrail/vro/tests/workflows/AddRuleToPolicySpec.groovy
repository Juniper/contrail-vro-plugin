/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.NetworkPolicy

class AddRuleToPolicySpec extends WorkflowSpec {

    def addRuleToNetworkPolicy = getWorkflowScript("Add rule to network policy")

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

    def "Adding rule to a network policy with existing rule list"() {
        given: "A correct set of attributes"
        def networkPolicy = dependencies.someNetworkPolicy()

        connectorMock.read(_) >> Status.success()

        when: "Running the script"
        invokeFunction(
            addRuleToNetworkPolicy,
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

        then: "The parent policy should be updated."
        1 * connectorMock.update({
            def _it = it as NetworkPolicy
            _it.uuid == networkPolicy.uuid &&
            _it.entries.policyRule.any{
                // TODO: how to check if a rule was added with 100% certainty?
                it.actionList.simpleAction == someSimpleAction &&
                it.protocol == someProtocol
            }}) >> Status.success()
    }
}
