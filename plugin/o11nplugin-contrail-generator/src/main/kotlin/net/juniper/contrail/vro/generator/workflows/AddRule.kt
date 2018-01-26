/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.generator.workflows.dsl.andParameters
import net.juniper.contrail.vro.generator.workflows.dsl.withScript
import net.juniper.contrail.vro.generator.workflows.model.FromStringParameter
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.model.boolean
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string

fun addRuleToPolicyWorkflow(info: ProjectInfo): Workflow {

    val workflowName = "Add rule to policy"

    return info.versionOf(workflowName) withScript addRuleToPolicyScriptBody andParameters {
        addRuleParams()
    }
}

fun ParameterAggregator.addRuleParams(suffix: String = "") {
    parameter("parent$suffix", NetworkPolicy::class.java.reference) {
        description = "Policy to add the rule to"
        mandatory = true
    }
    parameter("action$suffix", string) {
        description = "Action"
        mandatory = true
        defaultValue = "pass"
        predefinedAnswers = listOf("pass", "deny")
    }
    parameter("protocol$suffix", string) {
        description = "Protocol"
        mandatory = true
        defaultValue = "any"
        predefinedAnswers = listOf("any", "tcp", "udp", "icmp", "icmp6")
    }
    parameter("direction$suffix", string) {
        description = "Direction"
        mandatory = true
        defaultValue = "<>"
        predefinedAnswers = listOf("<>", ">")
    }
    parameter("src_address_type$suffix", string) {
        description = "Traffic Source"
        mandatory = true
        defaultValue = "CIDR"
        predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
    }
    parameter("src_address_cidr$suffix", string) {
        description = "Source CIDR"
        visibility = FromStringParameter("src_address_type$suffix", "CIDR")
    }
    parameter("src_address_network$suffix", VirtualNetwork::class.java.simpleName.reference) {
        description = "Source Virtual Network"
        visibility = FromStringParameter("src_address_type$suffix", "Network")
    }
    parameter("src_address_policy$suffix", NetworkPolicy::class.java.simpleName.reference) {
        description = "Source Network Policy"
        visibility = FromStringParameter("src_address_type$suffix", "Policy")
    }
    parameter("src_address_security_group$suffix", SecurityGroup::class.java.simpleName.reference) {
        description = "Source Security Group"
        visibility = FromStringParameter("src_address_type$suffix", "Security Group")
    }
    parameter("src_ports$suffix", string) {
        description = "Port"
        mandatory = true
        defaultValue = "any"
    }
    parameter("dst_address_type$suffix", string) {
        description = "Traffic Destination"
        mandatory = true
        defaultValue = "CIDR"
        predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
    }
    parameter("dst_address_cidr$suffix", string) {
        description = "Destination CIDR"
        visibility = FromStringParameter("dst_address_type$suffix", "CIDR")
    }
    parameter("dst_address_network$suffix", VirtualNetwork::class.java.simpleName.reference) {
        description = "Destination Virtual Network"
        visibility = FromStringParameter("dst_address_type$suffix", "Network")
    }
    parameter("dst_address_policy$suffix", NetworkPolicy::class.java.simpleName.reference) {
        description = "Destination Network Policy"
        visibility = FromStringParameter("dst_address_type$suffix", "Policy")
    }
    parameter("dst_address_security_group$suffix", SecurityGroup::class.java.simpleName.reference) {
        description = "Destination Security Group"
        visibility = FromStringParameter("dst_address_type$suffix", "Security Group")
    }
    parameter("dst_ports$suffix", string) {
        description = "Port"
        mandatory = true
        defaultValue = "any"
    }
    parameter("log$suffix", boolean) {
        description = "Log"
        mandatory = true
        defaultValue = false
    }
    parameter("services$suffix", boolean) {
        // TODO choose service
        description = "Services"
        mandatory = true
        defaultValue = false
    }
    parameter("mirror$suffix", boolean) {
        // TODO mirror settings
        description = "Mirror"
        mandatory = true
        defaultValue = false
    }
    parameter("QoS$suffix", boolean) {
        // TODO choose QoS
        description = "QoS"
        mandatory = true
        defaultValue = false
    }
}

// TODO: update "actions" with Services, Mirror, QoS
private val addRuleToPolicyScriptBody = """

var rule_sequence = new ContrailSequenceType(-1, -1);

var ruleUuid = ContrailUtils.randomUUID();

var srcPorts = ContrailUtils.parsePorts(src_ports);
var dstPorts = ContrailUtils.parsePorts(dst_ports);

var application = [];

var srcAddr = [ContrailUtils.createAddress(src_address_type, src_address_cidr, src_address_network, src_address_policy)];
var dstAddr = [ContrailUtils.createAddress(dst_address_type, dst_address_cidr, dst_address_network, dst_address_policy)];

var actions = new ContrailActionListType(ContrailUtils.lowercase(action));

var rule = new ContrailPolicyRuleType(rule_sequence, ruleUuid, direction, ContrailUtils.lowercase(protocol), srcAddr, srcPorts, application, dstAddr, dstPorts, actions);

var id = parent.getInternalId().toString();
var executor = ContrailConnectionManager.getExecutor(id);

parent.getEntries().addPolicyRule(rule);

executor.updateNetworkPolicy(parent);
""".trimIndent()