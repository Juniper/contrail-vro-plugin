/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.getNetworkPolicyRules
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.MultiAddressSecurityGroupRule
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.simpleTypeQualifiers
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

private val addressTypeParameterName = "addressType"
private val defaultPort = "any"
// There is no information about protocols in the schema
private val defaultProtocol = "any"
private val allowedProtocols = listOf("any", "tcp", "udp", "icmp", "icmp6")
private val defaultAddressType = "CIDR"
private val allowedAddressTypes = listOf("CIDR", "Security Group")
private val defaultDirection = "ingress"
private val allowedDirections = listOf("ingress", "egress")

internal fun addRuleToSecurityGroupWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add rule to security group"

    return customWorkflow<SecurityGroup>(workflowName).withScriptFile("addRuleToSecurityGroup") {
        step("Parent security group") {
            parameter("parent", reference<SecurityGroup>()) {
                extractRelationDescription<Project, SecurityGroup>(schema)
                mandatory = true
            }
        }
        step("Rule attributes") {
            visibility = WhenNonNull("parent")
            parameter("direction", string) {
                // direction has no description in the schema
                description = "Direction"
                mandatory = true
                defaultValue = defaultDirection
                predefinedAnswers = allowedDirections
            }
            parameter("ethertype", string) {
                // etherType has no description in the schema
                description = "Ether Type"
                additionalQualifiers += schema.simpleTypeQualifiers<PolicyRuleType>("ethertype")
            }
            parameter(addressTypeParameterName, string) {
                description = "Address Type"
                mandatory = true
                defaultValue = defaultAddressType
                predefinedAnswers = allowedAddressTypes
            }
            parameter("addressCidr", string) {
                description = schema.propertyDescription<AddressType>("subnet")
                mandatory = true
                visibility = FromStringParameter(addressTypeParameterName, "CIDR")
            }
            parameter("addressSecurityGroup", reference<SecurityGroup>()) {
                description = schema.propertyDescription<AddressType>("security-group")
                mandatory = true
                visibility = FromStringParameter(addressTypeParameterName, "Security Group")
            }
            parameter("protocol", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = defaultProtocol
                predefinedAnswers = allowedProtocols
            }
            parameter("ports", string) {
                description = "Port Range"
                mandatory = true
                defaultValue = defaultPort
            }
        }
    }
}

// TODO: Add data bindings
internal fun editSecurityGroupRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Edit security group rule"

    return customWorkflow<SecurityGroup>(workflowName).withScriptFile("editSecurityGroupRule") {
        step("Rule") {
            parameter(parent, reference<SecurityGroup>()) {
                extractRelationDescription<Project, SecurityGroup>(schema)
                mandatory = true
            }
            parameter("rule", string) {
                visibility = WhenNonNull(parent)
                description = "Rule to edit"
                predefinedAnswersFrom = actionCallTo(getNetworkPolicyRules).parameter(parent)
                customValidation = MultiAddressSecurityGroupRule(parent)
            }
        }
        step("Rule attributes") {
            visibility = WhenNonNull(parent)
            parameter("direction", string) {
                // direction has no description in the schema
                description = "Direction"
                mandatory = true
                defaultValue = defaultDirection
                predefinedAnswers = allowedDirections
            }
            parameter("ethertype", string) {
                // etherType has no description in the schema
                description = "Ether Type"
                additionalQualifiers += schema.simpleTypeQualifiers<PolicyRuleType>("ethertype")
            }
            parameter(addressTypeParameterName, string) {
                description = "Address Type"
                mandatory = true
                defaultValue = defaultAddressType
                predefinedAnswers = allowedAddressTypes
            }
            parameter("addressCidr", string) {
                description = schema.propertyDescription<AddressType>("subnet")
                mandatory = true
                visibility = FromStringParameter(addressTypeParameterName, "CIDR")
            }
            parameter("addressSecurityGroup", reference<SecurityGroup>()) {
                description = schema.propertyDescription<AddressType>("security-group")
                mandatory = true
                visibility = FromStringParameter(addressTypeParameterName, "Security Group")
            }
            parameter("protocol", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = defaultProtocol
                predefinedAnswers = allowedProtocols
            }
            parameter("ports", string) {
                description = "Port Range"
                mandatory = true
                defaultValue = defaultPort
            }
        }
    }
}

internal fun removeSecurityGroupRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove security group rule"

    return customWorkflow<SecurityGroup>(workflowName).withScriptFile("removeRuleFromSecurityGroup") {
        parameter(parent, reference<SecurityGroup>()) {
            extractRelationDescription<Project, SecurityGroup>(schema)
            mandatory = true
        }
        parameter("rule", string) {
            visibility = WhenNonNull(parent)
            description = "Rule to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(getNetworkPolicyRules).parameter(parent)
        }
    }
}