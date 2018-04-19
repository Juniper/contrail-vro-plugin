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
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.dsl.FromBooleanParameter
import net.juniper.contrail.vro.workflows.dsl.FromStringParameter
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.and
import net.juniper.contrail.vro.workflows.dsl.fromRuleProperty
import net.juniper.contrail.vro.workflows.dsl.or
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

private val sourceVirtualNetworkType = "srcVirtualNetworkType"
private val destinationVirtualNetworkType = "dstVirtualNetworkType"
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
            parameter(parent, reference<NetworkPolicy>()) {
                description = relationDescription<Project, NetworkPolicy>(schema)
                mandatory = true
            }
        }
        policyRuleParameters(schema, "parent", false)
    }
}

internal fun editPolicyRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Edit rule of network policy"

    return customWorkflow<NetworkPolicy>(workflowName).withScriptFile("editPolicyRule") {
        step("Rule") {
            parameter(parent, reference<NetworkPolicy>()) {
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

private fun PresentationParametersBuilder.policyRuleParameters(schema: Schema, visibilityDependencyField: String, loadCurrentValues: Boolean) {
    step("Basic attributes") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("simpleAction", string) {
            description = propertyDescription<ActionListType>(schema)
            additionalQualifiers += schema.simpleTypeConstraints<ActionListType>("simpleAction")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("protocol", string) {
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultProtocol
            predefinedAnswers = allowedProtocols
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("direction", string) {
            // direction has no description in the schema
            description = "Direction"
            mandatory = true
            additionalQualifiers += schema.simpleTypeConstraints<PolicyRuleType>("direction")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
    }

    step("Addresses") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter(sourceAddressTypeParameterName, string) {
            description = "Traffic Source"
            mandatory = true
            defaultValue = defaultAddressType
            predefinedAnswers = allowedAddressTypes
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("srcSubnet", string) {
            description = schema.propertyDescription<AddressType>("subnet")
            mandatory = true
            visibility = FromStringParameter(sourceAddressTypeParameterName, "CIDR")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter(sourceVirtualNetworkType, string) {
            description = "Type of source network address"
            mandatory = true
            visibility = FromStringParameter(sourceAddressTypeParameterName, "Network")
            defaultValue = defaultNetworkType
            predefinedAnswers = allowedNetworkTypes
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("srcVirtualNetwork", reference<VirtualNetwork>()) {
            description = schema.propertyDescription<AddressType>("virtual_network")
            mandatory = true
            visibility = FromStringParameter(sourceVirtualNetworkType, "reference")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("srcNetworkPolicy", reference<NetworkPolicy>()) {
            description = schema.propertyDescription<AddressType>("network-policy")
            mandatory = true
            visibility = FromStringParameter(sourceAddressTypeParameterName, "Policy")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("srcSecurityGroup", reference<SecurityGroup>()) {
            description = schema.propertyDescription<AddressType>("security-group")
            mandatory = true
            visibility = FromStringParameter(sourceAddressTypeParameterName, "Security Group")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("srcPorts", string) {
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultPort
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter(destinationAddressTypeParameterName, string) {
            description = "Traffic Destination"
            mandatory = true
            defaultValue = defaultAddressType
            predefinedAnswers = allowedAddressTypes
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("dstSubnet", string) {
            description = schema.propertyDescription<AddressType>("subnet")
            mandatory = true
            visibility = FromStringParameter(destinationAddressTypeParameterName, "CIDR")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter(destinationVirtualNetworkType, string) {
            description = "Type of destination network address"
            mandatory = true
            visibility = FromStringParameter(destinationAddressTypeParameterName, "Network")
            defaultValue = defaultNetworkType
            predefinedAnswers = allowedNetworkTypes
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("dstVirtualNetwork", reference<VirtualNetwork>()) {
            description = schema.propertyDescription<AddressType>("virtual_network")
            mandatory = true
            visibility = FromStringParameter(destinationVirtualNetworkType, "reference")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("dstNetworkPolicy", reference<NetworkPolicy>()) {
            description = schema.propertyDescription<AddressType>("network-policy")
            mandatory = true
            visibility = FromStringParameter(destinationAddressTypeParameterName, "Policy")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("dstSecurityGroup", reference<SecurityGroup>()) {
            description = schema.propertyDescription<AddressType>("security-group")
            mandatory = true
            visibility = FromStringParameter(destinationAddressTypeParameterName, "Security Group")
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("dstPorts", string) {
            description = propertyDescription<PolicyRuleType>(schema)
            mandatory = true
            defaultValue = defaultPort
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
    }

    step("Advanced Options") {
        visibility = WhenNonNull(visibilityDependencyField)
        parameter("log", boolean) {
            description = propertyDescription<ActionListType>(schema)
            mandatory = true
            defaultValue = false
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter(defineServicesParameterName, boolean) {
            description = "Services"
            mandatory = true
            defaultValue = false
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }

        parameter(defineMirrorParameterName, boolean) {
            description = "Mirror"
            mandatory = true
            defaultValue = false
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
    }

    step("Services") {
        visibility = FromBooleanParameter(defineServicesParameterName)
        parameter("services", array(reference<ServiceInstance>())) {
            description = "Service instances"
            mandatory = true
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
    }

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
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorAnalyzerInstance", reference<ServiceInstance>()) {
            description = "Analyzer Instance"
            mandatory = true
            visibility = mirrorIsAnalyzerInstance
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorAnalyzerName", string) {
            description = "Analyzer Name"
            mandatory = true
            visibility = mirrorIsNicAssisted or mirrorIsAnalyzerIp
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorNicAssistedVlan", number) {
            description = "NIC Assisted VLAN"
            mandatory = true
            min = 1
            max = 4094
            visibility = mirrorIsNicAssisted
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorAnalyzerIP", string) {
            description = "Analyzer IP"
            mandatory = true
            visibility = mirrorIsAnalyzerIp
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorAnalyzerMac", string) {
            description = "Analyzer MAC"
            mandatory = true
            visibility = mirrorIsAnalyzerIp
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorUdpPort", number) {
            description = "UDP Port"
            mandatory = true
            visibility = mirrorIsAnalyzerIp
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorJuniperHeader", string) {
            description = "Juniper Header"
            mandatory = true
            defaultValue = defaultJuniperHeaderOption
            predefinedAnswers = allowedJuniperHeaderOptions
            visibility = mirrorIsAnalyzerIp
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorRoutingInstance", reference<VirtualNetwork>()) {
            description = "Routing Instance"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and juniperHeaderIsDisabled
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorNexthopMode", string) {
            description = "Nexthop Mode"
            mandatory = true
            defaultValue = defaultNexthopMode
            predefinedAnswers = allowedNexthopModes
            visibility = mirrorIsAnalyzerIp
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorVtepDestIp", string) {
            description = "VTEP Dest IP"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and nextHopModeIsStatic
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorVtepDestMac", string) {
            description = "VTEP Dest MAC"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and nextHopModeIsStatic
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
        parameter("mirrorVni", number) {
            description = "VxLAN ID"
            mandatory = true
            visibility = mirrorIsAnalyzerIp and nextHopModeIsStatic
            if (loadCurrentValues) dataBinding = networkPolicyRulePropertyDataBinding()
        }
    }
}

internal fun removePolicyRuleWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove rule from network policy"

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

// default is capitalized to fit the camelCase function name
// e.g. srcPorts -> rulePropertySrcPorts
private fun<T : Any> BasicParameterBuilder<T>.networkPolicyRulePropertyDataBinding() =
    fromRuleProperty(
        parent,
        "rule",
        parameterName.capitalize(),
        type
    )