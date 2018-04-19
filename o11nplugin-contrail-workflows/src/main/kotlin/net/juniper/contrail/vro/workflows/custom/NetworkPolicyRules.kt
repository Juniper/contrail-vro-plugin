/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PolicyRuleType
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.networkPolicyRules
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.FromBooleanParameter
import net.juniper.contrail.vro.workflows.dsl.FromListPropertyValue
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.and
import net.juniper.contrail.vro.workflows.dsl.or
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

private val sourceAddressTypeParameterName = "srcAddressType"
private val destinationAddressTypeParameterName = "dstAddressType"
private val defineServicesParameterName = "defineServices"
private val defineMirrorParameterName = "defineMirror"
private val mirrorTypeParameterName = "mirrorType"
// There is no information about protocols in the schema
private val defaultPort = "any"
private val defaultProtocol = "any"
private val allowedProtocols = listOf("any", "tcp", "udp", "icmp", "icmp6")
private val defaultAddressType = "Network"
private val allowedAddressTypes = listOf("CIDR", "Network", "Policy", "Security Group")
private val defaultDirection = "<>"
private val allowedDirections = listOf("<>", ">")
private val defaultMirrorType = "Analyzer Instance"
private val allowedMirrorTypes = listOf("Analyzer Instance", "NIC Assisted", "Analyzer IP")
private val defaultJuniperHeaderOption = "enabled"
private val allowedJuniperHeaderOptions = listOf("enabled", "disabled")
private val defaultNexthopMode = "dynamic"
private val allowedNexthopModes = listOf("dynamic", "static")
private val defaultNetworkType = "any"
private val allowedNetworkTypes = listOf("any", "local", "reference")

internal fun addRuleToPolicyWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add rule to network policy"

    return customWorkflow<NetworkPolicy>(workflowName).withScriptFile("addRuleToPolicy") {
        step("Parent policy") {
            parameter("parent", reference<NetworkPolicy>()) {
                description = relationDescription<Project, NetworkPolicy>(schema)
                mandatory = true
            }
        }
        policyRuleParameters(schema, "parent", false)
    }
}

// TODO: add data bindings for complex-type attributes
internal fun editPolicyRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Edit network policy rule"

    return customWorkflow<NetworkPolicy>(workflowName).withScriptFile("editPolicyRule") {
        step("Rule") {
            parameter("parent", reference<NetworkPolicy>()) {
                description = relationDescription<Project, NetworkPolicy>(schema)
                mandatory = true
            }
            parameter("rule", string) {
                visibility = WhenNonNull(parent)
                description = "Rule to edit"
                predefinedAnswersFrom = actionCallTo(networkPolicyRules).parameter(parent)
                validWhen = isSingleAddressNetworkPolicyRuleOf(parent)
            }
        }
        policyRuleParameters(schema, "rule", true)
    }
}

internal fun PresentationParametersBuilder.policyRuleParameters(schema: Schema, visibilityDependencyField: String, loadCurrentValues: Boolean) {
    val policyRuleListGetter = "getEntries().getPolicyRule()"

    step("Basic attributes") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("simpleAction", string) {
            description = propertyDescription<ActionListType>(schema)
            additionalQualifiers += schema.simpleTypeConstraints<ActionListType>("simpleAction")
            if (loadCurrentValues) dataBinding = FromListPropertyValue(
                "parent",
                "rule",
                policyRuleListGetter,
                "getActionList().getSimpleAction()",
                string)
        }
        parameter("protocol", string) {
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultProtocol
            predefinedAnswers = allowedProtocols
            if (loadCurrentValues) dataBinding = FromListPropertyValue(
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
            additionalQualifiers += schema.simpleTypeConstraints<PolicyRuleType>("direction")
            if (loadCurrentValues) dataBinding = FromListPropertyValue(
                "parent",
                "rule",
                policyRuleListGetter,
                "direction",
                string)
        }
    }

    // TODO: bindings
    step("Addresses") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter(sourceAddressTypeParameterName, string) {
            description = "Traffic Source"
            mandatory = true
            defaultValue = defaultAddressType
            predefinedAnswers = allowedAddressTypes
        }
        parameter("srcAddressCidr", string) {
            description = schema.propertyDescription<AddressType>("subnet")
            mandatory = true
            visibility = FromStringParameter(sourceAddressTypeParameterName, "CIDR")
        }
        parameter("srcAddressNetworkType", string) {
            description = "Type of source network address"
            mandatory = true
            visibility = FromStringParameter(sourceAddressTypeParameterName, "Network")
            defaultValue = defaultNetworkType
            predefinedAnswers = allowedNetworkTypes
        }
        parameter("srcAddressNetwork", reference<VirtualNetwork>()) {
            description = schema.propertyDescription<AddressType>("virtual_network")
            mandatory = true
            visibility = FromStringParameter("srcAddressNetworkType", "reference")
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
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultPort
        }
        parameter(destinationAddressTypeParameterName, string) {
            description = "Traffic Destination"
            mandatory = true
            defaultValue = defaultAddressType
            predefinedAnswers = allowedAddressTypes
        }
        parameter("dstAddressCidr", string) {
            description = schema.propertyDescription<AddressType>("subnet")
            mandatory = true
            visibility = FromStringParameter(destinationAddressTypeParameterName, "CIDR")
        }
        parameter("dstAddressNetworkType", string) {
            description = "Type of destination network address"
            mandatory = true
            visibility = FromStringParameter(destinationAddressTypeParameterName, "Network")
            defaultValue = defaultNetworkType
            predefinedAnswers = allowedNetworkTypes
        }
        parameter("dstAddressNetwork", reference<VirtualNetwork>()) {
            description = schema.propertyDescription<AddressType>("virtual_network")
            mandatory = true
            visibility = FromStringParameter("dstAddressNetworkType", "reference")
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
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultPort
        }
    }

    // TODO: bindings
    step("Advanced Options") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("log", boolean) {
            description = propertyDescription<ActionListType>(schema)
            mandatory = true
            defaultValue = false
        }
        parameter(defineServicesParameterName, boolean) {
            description = "Services"
            mandatory = true
            defaultValue = false
        }

        parameter(defineMirrorParameterName, boolean) {
            description = "Mirror"
            mandatory = true
            defaultValue = false
        }
    }

    // TODO: bindings
    step("Services") {
        visibility = FromBooleanParameter(defineServicesParameterName)
        parameter("serviceInstances", array(reference<ServiceInstance>())) {
            description = "Service instances"
            mandatory = true
        }
    }

    // TODO: bindings
    step("Mirror") {
        visibility = FromBooleanParameter(defineMirrorParameterName)

        val mirrorIsAnalyzerInstance = FromStringParameter(mirrorTypeParameterName, "Analyzer Instance")
        val mirrorIsAnalyzerIp = FromStringParameter(mirrorTypeParameterName, "Analyzer IP")
        val mirrorIsNicAssisted = FromStringParameter(mirrorTypeParameterName, "NIC Assisted")
        val juniperHeaderIsDisabled = FromStringParameter("juniperHeader", "disabled")
        val nextHopModeIsStatic = FromStringParameter("nexthopMode", "static")

        parameter(mirrorTypeParameterName, string) {
            description = "Mirror Type"
            mandatory = true
            defaultValue = defaultMirrorType
            predefinedAnswers = allowedMirrorTypes
        }
        parameter("analyzerInstance", reference<ServiceInstance>()) {
            description = "Analyzer Instance"
            mandatory = true
            visibility = mirrorIsAnalyzerInstance
        }
        parameter("analyzerName", string) {
            description = "Analyzer Name"
            mandatory = true
            visibility = mirrorIsNicAssisted or mirrorIsAnalyzerIp
        }
        parameter("nicAssistedVlan", number) {
            description = "NIC Assisted VLAN"
            mandatory = true
            min = 1
            max = 4094
            visibility = mirrorIsNicAssisted
        }
        parameter("analyzerIP", string) {
            description = "Analyzer IP"
            mandatory = true
            visibility = mirrorIsAnalyzerIp
        }
        parameter("analyzerMac", string) {
            description = "Analyzer MAC"
            mandatory = true
            visibility = mirrorIsAnalyzerIp
        }
        parameter("udpPort", number) {
            description = "UDP Port"
            mandatory = true
            visibility = mirrorIsAnalyzerIp
        }
        parameter("juniperHeader", string) {
            description = "Juniper Header"
            mandatory = true
            defaultValue = defaultJuniperHeaderOption
            predefinedAnswers = allowedJuniperHeaderOptions
            visibility = mirrorIsAnalyzerIp
        }
        parameter("routingInstance", reference<VirtualNetwork>()) {
            description = "Routing Instance"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and juniperHeaderIsDisabled
        }
        parameter("nexthopMode", string) {
            description = "Nexthop Mode"
            mandatory = true
            defaultValue = defaultNexthopMode
            predefinedAnswers = allowedNexthopModes
            visibility = mirrorIsAnalyzerIp
        }
        parameter("vtepDestIp", string) {
            description = "VTEP Dest IP"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and nextHopModeIsStatic
        }
        parameter("vtepDestMac", string) {
            description = "VTEP Dest MAC"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and nextHopModeIsStatic
        }
        parameter("vni", number) {
            description = "VxLAN ID"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and nextHopModeIsStatic
        }
    }
}

internal fun removePolicyRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove network policy rule"

    return customWorkflow<NetworkPolicy>(workflowName).withScriptFile("removeRuleFromPolicy") {
        parameter(parent, reference<NetworkPolicy>()) {
            description = relationDescription<Project, NetworkPolicy>(schema)
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