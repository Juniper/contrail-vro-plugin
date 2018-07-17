/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.config.constants.addRuleToSecurityGroupWorkflowName
import net.juniper.contrail.vro.config.constants.editRuleOfSecurityGroupWorkflowName
import net.juniper.contrail.vro.config.constants.removeRuleFromSecurityGroupWorkflowName
import net.juniper.contrail.vro.config.constants.egress
import net.juniper.contrail.vro.config.constants.ingress
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.networkPolicyRules
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

private val addressTypeParameterName = "addressType"
private val defaultPort = "any"
// There is no information about protocols in the schema
private val defaultProtocol = "any"
private val allowedProtocols = listOf("any", "tcp", "udp", "icmp", "icmp6")
private val defaultAddressType = "CIDR"
private val allowedAddressTypes = listOf("CIDR", "Security Group")
private val defaultDirection = ingress
private val allowedDirections = listOf(ingress, egress)

internal fun addRuleToSecurityGroupWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<SecurityGroup>(addRuleToSecurityGroupWorkflowName).withScriptFile("addRuleToSecurityGroup") {
        step("Security Group") {
            parameter(item, reference<SecurityGroup>()) {
                description = relationDescription<Project, SecurityGroup>(schema)
                mandatory = true
            }
        }
        securityGroupRuleParameters(schema, item, false)
    }

internal fun editSecurityGroupRuleWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<SecurityGroup>(editRuleOfSecurityGroupWorkflowName).withScriptFile("editSecurityGroupRule") {
        step("Security Group Rule") {
            parameter(item, reference<SecurityGroup>()) {
                description = relationDescription<Project, SecurityGroup>(schema)
                mandatory = true
            }
            parameter(rule, string) {
                visibility = WhenNonNull(item)
                description = "Rule to edit"
                mandatory = true
                predefinedAnswersFrom = actionCallTo(networkPolicyRules).parameter(item)
                validWhen = isSingleAddressSecurityGroupRuleOf(item)
            }
        }
        securityGroupRuleParameters(schema, rule, true)
    }

private fun PresentationParametersBuilder.securityGroupRuleParameters(schema: Schema, visibilityDependencyField: String, loadCurrentValues: Boolean) {
    step("Rule properties") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("direction", string) {
            // direction has no description in the schema
            description = "Direction"
            mandatory = true
            defaultValue = defaultDirection
            predefinedAnswers = allowedDirections
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
        parameter("ethertype", string) {
            // etherType has no description in the schema
            description = "Ether Type"
            additionalQualifiers += schema.simpleTypeConstraints<PolicyRuleType>("ethertype")
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
        parameter(addressTypeParameterName, string) {
            description = "Address Type"
            mandatory = true
            defaultValue = defaultAddressType
            predefinedAnswers = allowedAddressTypes
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
        parameter("addressCidr", string) {
            description = schema.propertyDescription<AddressType>("subnet")
            mandatory = true
            visibility = FromStringParameter(addressTypeParameterName, "CIDR")
            validWhen = isCidr()
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
        parameter("addressSecurityGroup", reference<SecurityGroup>()) {
            description = schema.propertyDescription<AddressType>("security-group")
            mandatory = true
            visibility = FromStringParameter(addressTypeParameterName, "Security Group")
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
        parameter("protocol", string) {
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultProtocol
            predefinedAnswers = allowedProtocols
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
        parameter("ports", string) {
            description = "Port Range"
            mandatory = true
            defaultValue = defaultPort
            if (loadCurrentValues) dataBinding = rulePropertyDataBinding()
        }
    }
}

internal fun removeSecurityGroupRuleWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<SecurityGroup>(removeRuleFromSecurityGroupWorkflowName).withScriptFile("removeRuleFromSecurityGroup") {
        parameter(item, reference<SecurityGroup>()) {
            description = relationDescription<Project, SecurityGroup>(schema)
            mandatory = true
        }
        parameter(rule, string) {
            visibility = WhenNonNull(item)
            description = "Rule to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(networkPolicyRules).parameter(item)
        }
    }