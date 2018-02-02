/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.QosConfig
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.ActionParameter
import net.juniper.contrail.vro.workflows.model.FromBooleanParameter
import net.juniper.contrail.vro.workflows.model.FromListPropertyValue
import net.juniper.contrail.vro.workflows.model.FromStringParameter
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.WhenNonNull
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

internal fun addRuleToPolicyWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add rule to network policy"

    val sourceAddressTypeParameterName = "srcAddressType"
    val destinationAddressTypeParameterName = "dstAddressType"

    return customWorkflow<NetworkPolicy>(workflowName).withScriptFile("addRuleToPolicy") {
        step("Parent policy") {
            parameter("parent", reference<NetworkPolicy>()) {
                extractRelationDescription<Project, NetworkPolicy>(schema)
                mandatory = true
            }
        }
        step("Basic attributes") {
            visibility = WhenNonNull("parent")
            parameter("simpleAction", string) {
                extractPropertyDescription<ActionListType>(schema)
                mandatory = true
                defaultValue = "pass"
                predefinedAnswers = listOf("pass", "deny")
            }
            parameter("protocol", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = "any"
                predefinedAnswers = listOf("any", "tcp", "udp", "icmp", "icmp6")
            }
            parameter("direction", string) {
                // direction has no description in the schema
                description = "Direction"
                mandatory = true
                defaultValue = "<>"
                predefinedAnswers = listOf("<>", ">")
            }
        }
        step("Addresses") {
            visibility = WhenNonNull("parent")
            parameter(sourceAddressTypeParameterName, string) {
                description = "Traffic Source"
                mandatory = true
                defaultValue = "CIDR"
                predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
            }
            parameter("srcAddressCidr", string) {
                description = schema.propertyDescription<AddressType>("subnet")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "CIDR")
            }
            parameter("srcAddressNetwork", reference<VirtualNetwork>()) {
                description = schema.propertyDescription<AddressType>("virtual_network")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "Network")
            }
            parameter("srcAddressPolicy", reference<NetworkPolicy>()) {
                description = schema.propertyDescription<AddressType>("network-policy")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "Policy")
            }
            parameter("srcAddressSecurityGroup", reference<SecurityGroup>()) {
                description = schema.propertyDescription<AddressType>("security-group")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "Security Group")
            }
            parameter("srcPorts", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = "any"
            }
            parameter(destinationAddressTypeParameterName, string) {
                description = "Traffic Destination"
                mandatory = true
                defaultValue = "CIDR"
                predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
            }
            parameter("dstAddressCidr", string) {
                description = schema.propertyDescription<AddressType>("subnet")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "CIDR")
            }
            parameter("dstAddressNetwork", reference<VirtualNetwork>()) {
                description = schema.propertyDescription<AddressType>("virtual_network")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "Network")
            }
            parameter("dstAddressPolicy", reference<NetworkPolicy>()) {
                description = schema.propertyDescription<AddressType>("network-policy")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "Policy")
            }
            parameter("dstAddressSecurityGroup", reference<SecurityGroup>()) {
                description = schema.propertyDescription<AddressType>("security-group")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "Security Group")
            }
            parameter("dstPorts", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = "any"
            }
        }
        step("Advanced Options") {
            visibility = WhenNonNull("parent")
            parameter("log", boolean) {
                extractPropertyDescription<ActionListType>(schema)
                mandatory = true
                defaultValue = false
            }
            parameter("services", boolean) {
                description = "Services"
                mandatory = true
                defaultValue = false
            }
            parameter("serviceInstances", array(reference<ServiceInstance>())) {
                description = "Service instances"
                mandatory = true
                visibility = FromBooleanParameter("services")
            }
            parameter("mirror", boolean) {
                // TODO mirror settings
                description = "Mirror"
                mandatory = true
                defaultValue = false
            }
            parameter("QoSShow", boolean) {
                description = "QoS"
                mandatory = true
                defaultValue = false
            }
            parameter("qos", reference<QosConfig>()) {
                description = "QoS"
                mandatory = true
                visibility = FromBooleanParameter("QoSShow")
            }
        }
    }
}

// TODO: add data bindings for complex-type attributes
internal fun editPolicyRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Edit network policy rule"

    val sourceAddressTypeParameterName = "srcAddressType"
    val destinationAddressTypeParameterName = "dstAddressType"
    val policyRuleListGetter = "getEntries().getPolicyRule()"

    return customWorkflow<NetworkPolicy>(workflowName).withScriptFile("editPolicyRule") {
        step("Rule") {
            parameter("parent", reference<NetworkPolicy>()) {
                extractRelationDescription<Project, NetworkPolicy>(schema)
                mandatory = true
            }
            parameter("rule", string) {
                visibility = WhenNonNull("parent")
                description = "Rule to edit"
                predefinedAnswersAction = actionCall(
                    "getNetworkPolicyRules",
                    "test.actions",
                    listOf("parent")
                )
            }
        }
        step("Basic Attributes") {
            visibility = WhenNonNull("rule")
            parameter("simpleAction", string) {
                extractPropertyDescription<ActionListType>(schema)
                mandatory = true
                defaultValue = "pass"
                predefinedAnswers = listOf("pass", "deny")
                dataBinding = FromListPropertyValue(
                    "parent",
                    "rule",
                    policyRuleListGetter,
                    "getActionList().getSimpleAction()",
                    string)
            }
            parameter("protocol", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = "any"
                predefinedAnswers = listOf("any", "tcp", "udp", "icmp", "icmp6")
                dataBinding = FromListPropertyValue(
                    "parent",
                    "rule",
                    policyRuleListGetter,
                    "protocol",
                    string)
            }
            parameter("direction", string) {
                // direction has no description in the schema
                description = "Direction"
                mandatory = true
                defaultValue = "<>"
                predefinedAnswers = listOf("<>", ">")
                dataBinding = FromListPropertyValue(
                    "parent",
                    "rule",
                    policyRuleListGetter,
                    "direction",
                    string)
            }
        }
        step("Addresses") {
            visibility = WhenNonNull("rule")
            parameter(sourceAddressTypeParameterName, string) {
                description = "Traffic Source"
                mandatory = true
                defaultValue = "CIDR"
                predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
            }
            parameter("srcAddressCidr", string) {
                description = schema.propertyDescription<AddressType>("subnet")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "CIDR")
            }
            parameter("srcAddressNetwork", reference<VirtualNetwork>()) {
                description = schema.propertyDescription<AddressType>("virtual-network")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "Network")
            }
            parameter("srcAddressPolicy", reference<NetworkPolicy>()) {
                description = schema.propertyDescription<AddressType>("network-policy")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "Policy")
            }
            parameter("srcAddressSecurityGroup", reference<SecurityGroup>()) {
                description = schema.propertyDescription<AddressType>("security-group")
                mandatory = true
                visibility = FromStringParameter(sourceAddressTypeParameterName, "Security Group")
            }
            parameter("srcPorts", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = "any"
            }
            parameter(destinationAddressTypeParameterName, string) {
                description = "Traffic Destination"
                mandatory = true
                defaultValue = "CIDR"
                predefinedAnswers = listOf("CIDR", "Network", "Policy", "Security Group")
            }
            parameter("dstAddressCidr", string) {
                description = schema.propertyDescription<AddressType>("subnet")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "CIDR")
            }
            parameter("dstAddressNetwork", reference<VirtualNetwork>()) {
                description = schema.propertyDescription<AddressType>("virtual-network")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "Network")
            }
            parameter("dstAddressPolicy", reference<NetworkPolicy>()) {
                description = schema.propertyDescription<AddressType>("network-policy")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "Policy")
            }
            parameter("dstAddressSecurityGroup", reference<SecurityGroup>()) {
                description = schema.propertyDescription<AddressType>("security-group")
                mandatory = true
                visibility = FromStringParameter(destinationAddressTypeParameterName, "Security Group")
            }
            parameter("dstPorts", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = "any"
            }
        }
        step("Advanced Options") {
            visibility = WhenNonNull("rule")
            parameter("log", boolean) {
                extractPropertyDescription<ActionListType>(schema)
                mandatory = true
                defaultValue = false
            }
            parameter("services", boolean) {
                description = "Services"
                mandatory = true
                defaultValue = false
            }
            parameter("serviceInstances", array(reference<ServiceInstance>())) {
                description = "Service instances"
                mandatory = true
                visibility = FromBooleanParameter("services")
            }
            parameter("mirror", boolean) {
                // TODO mirror settings
                description = "Mirror"
                mandatory = true
                defaultValue = false
            }
            parameter("QoSShow", boolean) {
                description = "QoS"
                mandatory = true
                defaultValue = false
            }
            parameter("qos", reference<QosConfig>()) {
                description = "QoS"
                mandatory = true
                visibility = FromBooleanParameter("QoSShow")
            }
        }
    }
}

private fun actionCall(name: String, packageName: String, arguments: List<String>): Action = Action(
    name,
    packageName,
    "",
    "",
    array(string),
    arguments.map { ActionParameter(it, string) },
    Script("")
)