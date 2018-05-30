/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyEntriesType
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.PortType

import static net.juniper.contrail.vro.config.Actions.networkPolicyRules
import static net.juniper.contrail.vro.model.UtilsKt.utils

class NetworkPolicyRulesSpec extends ActionSpec {
    def action = actionFromScript(networkPolicyRules)

    def "null network policy results in empty list" () {
        given: "null network policy"
        def policy = null

        when: "retrieved list of rules"
        def result = invokeAction(action, policy) as List<String>

        then:
        result.isEmpty()
    }

    def "empty network policy results in empty list" () {
        given: "empty policy"
        def policy = new NetworkPolicy()

        when: "retrieved list of rules"
        def result = invokeAction(action, policy) as List<String>

        then:
        result.isEmpty()
    }

    def "network policy with empty rule list list results in empty list" () {
        given: "network policy with empty list of rules"
        def policy = new NetworkPolicy()
        def rules = new PolicyEntriesType()
        policy.entries = rules

        when: "retrieved list of rules"
        def result = invokeAction(action, policy) as List<String>

        then:
        result.isEmpty()
    }

    def "network policy with single rule results in list with single formatted rule" () {
        given: "network policy with single rule"
        def policy = new NetworkPolicy()
        def rules = new PolicyEntriesType()
        policy.entries = rules
        def rule = new PolicyRuleType()
        rules.addPolicyRule(rule)
        rule.actionList = new ActionListType("deny")
        rule.addSrcAddresses(new AddressType())
        rule.addDstAddresses(new AddressType())
        rule.addSrcPorts(new PortType())
        rule.addDstPorts(new PortType())

        when: "retrieved list of rules"
        def result = invokeAction(action, policy) as List<String>

        then: "resulting list has one formatted element"
        result.size() == 1
        result[0] == utils.ruleToString(rule, 0)
    }
}
