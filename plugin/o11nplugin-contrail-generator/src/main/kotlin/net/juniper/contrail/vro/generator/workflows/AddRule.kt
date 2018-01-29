/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.FromStringParameter
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.loadFile

fun addRuleToPolicyWorkflow(info: ProjectInfo): WorkflowDefinition {

    val workflowName = "Add rule to policy"

    return workflow(workflowName).withScript(loadFile(info.generatorRoot, "addRuleToPolicy")) {
        parameter("parent", reference<NetworkPolicy>()) {
            description = "Policy to add the rule to"
            mandatory = true
        }
        parameter("action", string) {
            description = "Action"
            mandatory = true
            defaultValue = "pass"
            predefinedAnswers = listOf("pass", "deny")
        }
        parameter("protocol", string) {
            description = "Protocol"
            mandatory = true
            defaultValue = "any"
            predefinedAnswers = listOf("any", "tcp", "udp", "icmp", "icmp6")
        }
        parameter("direction", string) {
            description = "Direction"
            mandatory = true
            defaultValue = "<>"
            predefinedAnswers = listOf("<>", ">")
        }
        parameter("src_address_type", string) {
            description = "Traffic Source"
            mandatory = true
            defaultValue = "CIDR"
            predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
        }
        parameter("src_address_cidr", string) {
            description = "Source CIDR"
            visibility = FromStringParameter("src_address_type", "CIDR")
        }
        parameter("src_address_network", reference<VirtualNetwork>()) {
            description = "Source Virtual Network"
            visibility = FromStringParameter("src_address_type", "Network")
        }
        parameter("src_address_policy", reference<NetworkPolicy>()) {
            description = "Source Network Policy"
            visibility = FromStringParameter("src_address_type", "Policy")
        }
        parameter("src_address_security_group", reference<SecurityGroup>()) {
            description = "Source Security Group"
            visibility = FromStringParameter("src_address_type", "Security Group")
        }
        parameter("src_ports", string) {
            description = "Port"
            mandatory = true
            defaultValue = "any"
        }
        parameter("dst_address_type", string) {
            description = "Traffic Destination"
            mandatory = true
            defaultValue = "CIDR"
            predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
        }
        parameter("dst_address_cidr", string) {
            description = "Destination CIDR"
            visibility = FromStringParameter("dst_address_type", "CIDR")
        }
        parameter("dst_address_network", reference<VirtualNetwork>()) {
            description = "Destination Virtual Network"
            visibility = FromStringParameter("dst_address_type", "Network")
        }
        parameter("dst_address_policy", reference<NetworkPolicy>()) {
            description = "Destination Network Policy"
            visibility = FromStringParameter("dst_address_type", "Policy")
        }
        parameter("dst_address_security_group", reference<SecurityGroup>()) {
            description = "Destination Security Group"
            visibility = FromStringParameter("dst_address_type", "Security Group")
        }
        parameter("dst_ports", string) {
            description = "Port"
            mandatory = true
            defaultValue = "any"
        }
        parameter("log", boolean) {
            description = "Log"
            mandatory = true
            defaultValue = false
        }
        parameter("services", boolean) {
            // TODO choose service
            description = "Services"
            mandatory = true
            defaultValue = false
        }
        parameter("mirror", boolean) {
            // TODO mirror settings
            description = "Mirror"
            mandatory = true
            defaultValue = false
        }
        parameter("QoS", boolean) {
            // TODO choose QoS
            description = "QoS"
            mandatory = true
            defaultValue = false
        }
    }
}
