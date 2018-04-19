/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.networkPolicyRules
import net.juniper.contrail.vro.config.propertyOfRuleOfSecurityGroup
import net.juniper.contrail.vro.workflows.dsl.FromAction
import net.juniper.contrail.vro.workflows.dsl.FromListPropertyValue
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

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
            parameter(parent, reference<SecurityGroup>()) {
                description = relationDescription<Project, SecurityGroup>(schema)
                mandatory = true
            }
        }
        securityGroupRuleParameters(schema, parent, false)
    }
}

internal fun editSecurityGroupRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Edit security group rule"

    return customWorkflow<SecurityGroup>(workflowName).withScriptFile("editSecurityGroupRule") {
        step("Rule") {
            parameter(parent, reference<SecurityGroup>()) {
                description = relationDescription<Project, SecurityGroup>(schema)
                mandatory = true
            }
            parameter("rule", string) {
                visibility = WhenNonNull(parent)
                description = "Rule to edit"
                predefinedAnswersFrom = actionCallTo(networkPolicyRules).parameter(parent)
                validWhen = isSingleAddressSecurityGroupRuleOf(parent)
            }
        }
        securityGroupRuleParameters(schema, "rule", true)
    }
}

// TODO: Add data bindings
internal fun PresentationParametersBuilder.securityGroupRuleParameters(schema: Schema, visibilityDependencyField: String, loadCurrentValues: Boolean) {
    val securityGroupRuleListGetter = "getEntries().getPolicyRule()"

    step("Rule attributes") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("direction", string) {
            // direction has no description in the schema
            description = "Direction"
            mandatory = true
            defaultValue = defaultDirection
            predefinedAnswers = allowedDirections
            if (loadCurrentValues) dataBinding = FromAction(actionCallTo(propertyOfRuleOfSecurityGroup)
                .parameter(parent)
                .parameter("rule")
                .string("direction")
                .create(), string)
        }
        parameter("ethertype", string) {
            // etherType has no description in the schema
            description = "Ether Type"
            additionalQualifiers += schema.simpleTypeConstraints<PolicyRuleType>("ethertype")
            if (loadCurrentValues) dataBinding = FromListPropertyValue(
                parent,
                "rule",
                securityGroupRuleListGetter,
                "ethertype",
                string)
        }
        parameter(addressTypeParameterName, string) {
            description = "Address Type"
            mandatory = true
            defaultValue = defaultAddressType
            predefinedAnswers = allowedAddressTypes
            if (loadCurrentValues) dataBinding = FromAction(actionCallTo(propertyOfRuleOfSecurityGroup)
                .parameter(parent)
                .parameter("rule")
                .string(addressTypeParameterName)
                .create(), string)
        }
        parameter("addressCidr", string) {
            description = schema.propertyDescription<AddressType>("subnet")
            mandatory = true
            visibility = FromStringParameter(addressTypeParameterName, "CIDR")
            if (loadCurrentValues) dataBinding = FromAction(actionCallTo(propertyOfRuleOfSecurityGroup)
                .parameter(parent)
                .parameter("rule")
                .string("addressCidr")
                .create(), string)
        }
        parameter("addressSecurityGroup", reference<SecurityGroup>()) {
            description = schema.propertyDescription<AddressType>("security-group")
            mandatory = true
            visibility = FromStringParameter(addressTypeParameterName, "Security Group")
            if (loadCurrentValues) dataBinding = FromAction(actionCallTo(propertyOfRuleOfSecurityGroup)
                .parameter(parent)
                .parameter("rule")
                .string("addressSecurityGroup")
                .create(), reference<SecurityGroup>())
        }
        parameter("protocol", string) {
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultProtocol
            predefinedAnswers = allowedProtocols
            if (loadCurrentValues) dataBinding = FromListPropertyValue(
                parent,
                "rule",
                securityGroupRuleListGetter,
                "protocol",
                string)
        }
        parameter("ports", string) {
            description = "Port Range"
            mandatory = true
            defaultValue = defaultPort
            if (loadCurrentValues) dataBinding = FromAction(actionCallTo(propertyOfRuleOfSecurityGroup)
                .parameter(parent)
                .parameter("rule")
                .string("ports")
                .create(), string)
        }
    }
}

internal fun removeSecurityGroupRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove security group rule"

    return customWorkflow<SecurityGroup>(workflowName).withScriptFile("removeRuleFromSecurityGroup") {
        parameter(parent, reference<SecurityGroup>()) {
            description = relationDescription<Project, SecurityGroup>(schema)
            mandatory = true
        }
        parameter("rule", string) {
            visibility = WhenNonNull(parent)
            description = "Rule to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(networkPolicyRules).parameter(parent)
        }
    }
}