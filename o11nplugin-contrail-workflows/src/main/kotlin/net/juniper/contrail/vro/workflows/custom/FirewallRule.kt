/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FirewallRuleEndpointType
import net.juniper.contrail.api.types.FirewallServiceType
import net.juniper.contrail.api.types.PolicyManagement
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

enum class EndpointType(val value: String) {
    None("none"),
    Tag("tag"),
    AddressGroup("addressGroup"),
    VirtualNetwork("virtualNetwork"),
    AnyWorkload("anyWorkload")
}
val defaultEndpointType = EndpointType.None.value
val allowedEndpointTypes = EndpointType.values().map { it.value }

const val serviceTypeParameterName = "serviceType"
enum class ServiceType(val value: String) {
    Manual("manual"),
    Reference("reference")
}
val defaultServiceType = ServiceType.Reference.value
val allowedServiceTypes = ServiceType.values().map { it.value }

val allowedMatchTags = listOf("Application", "Tier", "Deployment", "Site")

internal fun createPolicyManagementFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Create firewall rule in policy management"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("createFirewallRule") {
        step("Parent") {
            description = "Policy management this firewall rule will belong to"
            parameter("parentPolicyManagement", reference<PolicyManagement>()) {
                mandatory = true
            }
        }
        firewallRuleParameters(schema, "parentPolicyManagement", false)
    }
}

internal fun createProjectFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Create firewall rule in project"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("createFirewallRule") {
        step("Parent") {
            description = "Project this firewall rule will belong to"
            parameter("parentProject", reference<Project>()) {
                mandatory = true
            }
        }
        firewallRuleParameters(schema, "parentProject", false)
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
        firewallRuleParameters(schema, "rule", true)

    }
}

private fun PresentationParametersBuilder.firewallRuleParameters(schema: Schema, visibilityDependencyField: String, loadCurrentValues: Boolean) {
    step("Basic attributes") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("action", string) {
            description = schema.propertyDescription<ActionListType>("simpleAction")
            additionalQualifiers += schema.simpleTypeConstraints<ActionListType>("simpleAction")
        }
        parameter("direction", string) {
            description = "Direction"
            mandatory = true
            additionalQualifiers += schema.simpleTypeConstraints<FirewallRule>("direction")
        }
    }
    step("Endpoints") {
        visibility = WhenNonNull(visibilityDependencyField)
        endpointParameters(schema, 1)
        endpointParameters(schema, 2)
    }
    step("Service") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter(serviceTypeParameterName, string) {
            mandatory = true
            predefinedAnswers = allowedServiceTypes
            defaultValue = defaultServiceType
        }
        parameter("serviceProtocol", string) {
            // todo: validation
            description = schema.propertyDescription<FirewallServiceType>("protocol")
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
        }
        parameter("serviceSourcePorts", string) {
            // todo: validation
            description = schema.propertyDescription<FirewallServiceType>("src-ports")
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
        }
        parameter("serviceDestinationPorts", string) {
            // todo: validation
            description = schema.propertyDescription<FirewallServiceType>("dst-ports")
            visibility = FromStringParameter(serviceTypeParameterName, ServiceType.Manual.value)
        }
        parameter("serviceReference", reference<ServiceGroup>()) {
            // todo: listing
            description = "Service group"
        }
    }
    step("Match Tags") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("matchTags", array(string)) {
            predefinedAnswers = allowedMatchTags
            sameValues = false
        }
    }
}

private fun ParameterAggregator.endpointParameters(schema: Schema, endpointNumber: Int) {
    val endpointName = endpointParameterName(endpointNumber)
    val endpointTypeParameterName = "${endpointName}Type"
    parameter(endpointTypeParameterName, string) {
        description = "End Point $endpointNumber type"
        mandatory = true
        predefinedAnswers = allowedEndpointTypes
        defaultValue = defaultEndpointType
    }
    parameter("${endpointName}Tags", array(reference<Tag>())) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("tags")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.Tag.value)
    }
    parameter("${endpointName}VirtualNetworks", reference<VirtualNetwork>()) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("virtual-network")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.VirtualNetwork.value)
    }
    parameter("${endpointName}AddressGroups", reference<AddressGroup>()) {
        description = schema.propertyDescription<FirewallRuleEndpointType>("address-group")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.AddressGroup.value)
    }
}

private fun endpointParameterName(endpointNumber: Int) = "endpoint$endpointNumber"