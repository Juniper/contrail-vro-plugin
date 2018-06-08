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

val parentConnectionField = "parentConnection"
val parentProjectField = "parentProject"

internal fun createPolicyManagementFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Create global firewall rule"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("createFirewallRule") {
        step("Parent") {
            parameter(parentConnectionField, Connection.reference) {
                description = "Contrail connection in which the rule will be created"
                mandatory = true
            }
        }
        output("rule", reference<FirewallRule>()) {
            description = "Rule created by this workflow"
        }
        // we use connection name, as it does not have an uuid.
        firewallRuleParameters(schema, "$parentConnectionField.name", parentConnectionField, false)
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
        output("rule", reference<FirewallRule>()) {
            description = "Rule created by this workflow"
        }
        firewallRuleParameters(schema, "$parentProjectField.uuid", parentProjectField, false)
    }
}

internal fun editFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Edit firewall rule"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("editFirewallRule") {
        step("Rule") {
            parameter("rule", reference<FirewallRule>()) {
                mandatory = true
            }
        }
        firewallRuleParameters(schema, "rule.parentUuid", "rule", true)

    }
}

private fun PresentationParametersBuilder.firewallRuleParameters(schema: Schema, parentField: String, visibilityDependencyField: String, loadCurrentValues: Boolean) {
    step("Basic attributes") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("action", string) {
            description = schema.propertyDescription<ActionListType>("simpleAction")
            additionalQualifiers += schema.simpleTypeConstraints<ActionListType>("simpleAction")
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("actionList.simpleAction")
        }
        parameter("direction", string) {
            description = "Direction"
            mandatory = true
            additionalQualifiers += schema.simpleTypeConstraints<FirewallRule>("direction")
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("direction")
        }
    }
    step("Endpoints") {
        visibility = WhenNonNull(visibilityDependencyField)
        endpointParameters(schema, parentField, 1, loadCurrentValues)
        endpointParameters(schema, parentField, 2, loadCurrentValues)
    }
    step("Service") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter(serviceTypeParameterName, string) {
            mandatory = true
            predefinedAnswers = allowedServiceTypes
            defaultValue = defaultServiceType
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("serviceType()")
        }
        parameter("serviceProtocol", string) {
            description = schema.propertyDescription<FirewallServiceType>("protocol")
            defaultValue = "any"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
            mandatory = true
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("service.protocol")
        }
        parameter("serviceSrcPorts", string) {
            description = schema.propertyDescription<FirewallServiceType>("src-ports")
            defaultValue = "any"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
            mandatory = true
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("serviceSrcPorts()")
        }
        parameter("serviceDstPorts", string) {
            description = schema.propertyDescription<FirewallServiceType>("dst-ports")
            defaultValue = "any"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
            mandatory = true
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("serviceDstPorts()")
        }
        parameter("serviceReference", reference<ServiceGroup>()) {
            description = "Service group"
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Reference.value)
            mandatory = true
            validWhen = matchesSecurityParentage(parentField)
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("serviceGroup[0]")
        }
    }
    step("Match Tags") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("matchTags", array(string)) {
            predefinedAnswers = allowedMatchTags
            sameValues = false
            if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("matchTags.tagList")
        }
    }
}

private fun ParameterAggregator.endpointParameters(schema: Schema, parentField: String, endpointNumber: Int, loadCurrentValues: Boolean) {
    val endpointName = endpointParameterName(endpointNumber)
    val endpointTypeParameterName = "${endpointName}Type"
    parameter(endpointTypeParameterName, string) {
        description = "End Point $endpointNumber type"
        mandatory = true
        predefinedAnswers = allowedEndpointTypes
        defaultValue = defaultEndpointType
        if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("endpointType($endpointNumber)")
    }
    parameter("${endpointName}Tags", array(reference<Tag>())) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("tags")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.Tag.value)
        mandatory = true
        sameValues = false
        validWhen = matchesSecurityParentage(parentField)
        if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("endpointTags($endpointNumber)")
    }
    parameter("${endpointName}VirtualNetwork", reference<VirtualNetwork>()) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("virtual-network")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.VirtualNetwork.value)
        mandatory = true
        if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("endpointNetwork($endpointNumber)")
    }
    parameter("${endpointName}AddressGroup", reference<AddressGroup>()) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("address-group")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.AddressGroup.value)
        mandatory = true
        if (loadCurrentValues) dataBinding = firewallRulePropertyDataBinding("endpointAddressGroup($endpointNumber)")
    }
}

private fun endpointParameterName(endpointNumber: Int) = "endpoint$endpointNumber"

private fun<T : Any> BasicParameterBuilder<T>.firewallRulePropertyDataBinding(path: String) =
    FromComplexPropertyValue("rule", path, type)
