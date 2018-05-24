/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FirewallRuleEndpointType
import net.juniper.contrail.api.types.FirewallServiceType
import net.juniper.contrail.api.types.SecurityLoggingObject
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.propertyDescription

enum class EndpointType(val value: String) {
    None("none"),
    Tag("tag"),
    AddressGroup("addressGroup"),
    VirtualNetwork("virtualNetwork"),
    AnyWorkload("anyWorkload")
}

val defaultEndpointType = EndpointType.None.value
val allowedEndpointTypes = EndpointType.values().map { it.value }

val serviceTypeParameterName = "serviceType"
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
        firewallRuleParameters(schema)
    }
}

internal fun createProjectFirewallRule(schema: Schema): WorkflowDefinition {

    val workflowName = "Create firewall rule in project"

    return customWorkflow<FirewallRule>(workflowName).withScriptFile("createFirewallRule") {
        firewallRuleParameters(schema)
    }
}

private fun PresentationParametersBuilder.firewallRuleParameters(schema: Schema) {
    step("Basic attributes") {
        parameter("action", string) {
            description = propertyDescription<ActionListType>(schema)
            additionalQualifiers += schema.simpleTypeConstraints<ActionListType>("simpleAction")
        }
        parameter("direction", string) {
            description = "Direction"
            mandatory = true
            additionalQualifiers += schema.simpleTypeConstraints<FirewallRule>("direction")
        }
    }
    step("Endpoints") {
        endpointParameters(schema, 1)
        endpointParameters(schema, 2)
    }
    step("Service") {
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
        parameter("matchTags", array(string)) {
            predefinedAnswers = allowedMatchTags
        }
    }
    step("Security Logging Objects") {
        parameter("securityLoggingObjects", array(reference<SecurityLoggingObject>())) {
            // todo: listing
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
        // todo: listing
        description = schema.propertyDescription<FirewallRuleEndpointType>("tags")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.Tag.value)
    }
    parameter("${endpointName}VirtualNetworks", reference<VirtualNetwork>()) {
        // todo: listing
        description = schema.propertyDescription<FirewallRuleEndpointType>("virtual-network")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.VirtualNetwork.value)
    }
    parameter("${endpointName}AddressGroups", reference<AddressGroup>()) {
        // todo: listing
        description = schema.propertyDescription<FirewallRuleEndpointType>("address-group")
        visibility = FromStringParameter(endpointTypeParameterName, EndpointType.AddressGroup.value)
    }
}

private fun endpointParameterName(endpointNumber: Int) = "endpoint$endpointNumber"