/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FirewallRuleEndpointType
import net.juniper.contrail.api.types.FirewallServiceType
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.EndpointType
import net.juniper.contrail.vro.config.constants.ServiceType
import net.juniper.contrail.vro.config.constants.rule
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.dsl.FromComplexPropertyValue
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

val defaultEndpointType = EndpointType.None.value
val allowedEndpointTypes = EndpointType.values().map { it.value }

const val serviceTypeParameterName = "serviceType"
val defaultServiceType = ServiceType.Manual.value
val allowedServiceTypes = ServiceType.values().map { it.value }

val allowedMatchTags = listOf("application", "tier", "deployment", "site")

const val parentConnectionField = "parentConnection"
const val parentProjectField = "parentProject"

internal fun createPolicyManagementFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Create global firewall rule"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("createFirewallRule") {
        step("Parent") {
            parameter(parentConnectionField, Connection.reference) {
                description = "Contrail connection in which the rule will be created"
                mandatory = true
            }
        }
        output(rule, reference<FirewallRule>()) {
            description = "Rule created by this workflow"
        }
        firewallRuleParameters(schema, parentConnectionField, false)
    }
}

internal fun createProjectFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Create firewall rule in project"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("createFirewallRule") {
        step("Parent") {
            parameter(parentProjectField, reference<Project>()) {
                description = "Project this firewall rule will belong to"
                mandatory = true
            }
        }
        output(rule, reference<FirewallRule>()) {
            description = "Rule created by this workflow"
        }
        firewallRuleParameters(schema, parentProjectField, false)
    }
}

internal fun editFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Edit firewall rule"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("editFirewallRule") {
        step("Rule") {
            parameter(rule, reference<FirewallRule>()) {
                mandatory = true
            }
        }
        firewallRuleParameters(schema, rule, true)

    }
}

private fun PresentationParametersBuilder.firewallRuleParameters(schema: Schema, parentField: String, editing: Boolean) {
    val projectValidationDirectMode = !editing
    step("Basic attributes") {
        visibility = WhenNonNull(parentField)
        parameter("action", string) {
            description = schema.propertyDescription<ActionListType>("simpleAction")
            additionalQualifiers += schema.simpleTypeConstraints<ActionListType>("simpleAction")
            if (editing) dataBinding = firewallRulePropertyDataBinding("actionList.simpleAction")
        }
        parameter("direction", string) {
            description = "Direction"
            mandatory = true
            additionalQualifiers += schema.simpleTypeConstraints<FirewallRule>("direction")
            if (editing) dataBinding = firewallRulePropertyDataBinding("direction")
        }
    }
    step("Endpoints") {
        visibility = WhenNonNull(parentField)
        endpointParameters(schema, parentField, 1, editing)
        endpointParameters(schema, parentField, 2, editing)
    }
    step("Service") {
        visibility = WhenNonNull(parentField)
        parameter(serviceTypeParameterName, string) {
            mandatory = true
            predefinedAnswers = allowedServiceTypes
            defaultValue = defaultServiceType
            if (editing) dataBinding = firewallRulePropertyDataBinding("serviceType()")
        }
        parameter("serviceProtocol", string) {
            description = schema.propertyDescription<FirewallServiceType>("protocol")
            defaultValue = "any"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
            mandatory = true
            if (editing) dataBinding = firewallRulePropertyDataBinding("service.protocol")
        }
        parameter("serviceSrcPorts", string) {
            description = schema.propertyDescription<FirewallServiceType>("src-ports")
            defaultValue = "any"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
            mandatory = true
            if (editing) dataBinding = firewallRulePropertyDataBinding("serviceSrcPorts()")
        }
        parameter("serviceDstPorts", string) {
            description = schema.propertyDescription<FirewallServiceType>("dst-ports")
            defaultValue = "any"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
            mandatory = true
            if (editing) dataBinding = firewallRulePropertyDataBinding("serviceDstPorts()")
        }
        parameter("serviceReference", reference<ServiceGroup>()) {
            description = "Service group"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Reference.value)
            mandatory = true
            validWhen = matchesSecurityScope(parentField, projectValidationDirectMode)
            if (editing) dataBinding = firewallRulePropertyDataBinding("serviceGroup[0]")
        }
    }
    step("Match Tags") {
        visibility = WhenNonNull(parentField)
        parameter("matchTags", array(string)) {
            predefinedAnswers = allowedMatchTags
            sameValues = false
            if (editing) dataBinding = firewallRulePropertyDataBinding("matchTags.tagList")
        }
    }
}

private fun ParameterAggregator.endpointParameters(schema: Schema, parentField: String, endpointNumber: Int, editing: Boolean) {
    val endpointName = endpointParameterName(endpointNumber)
    val endpointTypeParameterName = "${endpointName}Type"
    val projectValidationDirectMode = !editing
    parameter(endpointTypeParameterName, string) {
        description = "End Point $endpointNumber type"
        mandatory = true
        predefinedAnswers = allowedEndpointTypes
        defaultValue = defaultEndpointType
        if (editing) dataBinding = firewallRulePropertyDataBinding("endpointType($endpointNumber)")
    }
    parameter("${endpointName}Tags", array(reference<Tag>())) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("tags")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.Tag.value)
        mandatory = true
        sameValues = false
        validWhen = matchesSecurityScope(parentField, projectValidationDirectMode)
        if (editing) dataBinding = firewallRulePropertyDataBinding("endpointTags($endpointNumber)")
    }
    parameter("${endpointName}VirtualNetwork", reference<VirtualNetwork>()) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("virtual-network")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.VirtualNetwork.value)
        mandatory = true
        if (editing) dataBinding = firewallRulePropertyDataBinding("endpointNetwork($endpointNumber)")
    }
    parameter("${endpointName}AddressGroup", reference<AddressGroup>()) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("address-group")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.AddressGroup.value)
        mandatory = true
        if (editing) dataBinding = firewallRulePropertyDataBinding("endpointAddressGroup($endpointNumber)")
    }
}

private fun endpointParameterName(endpointNumber: Int) = "endpoint$endpointNumber"

private fun<T : Any> BasicParameterBuilder<T>.firewallRulePropertyDataBinding(path: String) =
    FromComplexPropertyValue(rule, path, type)
