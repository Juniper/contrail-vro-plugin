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
import net.juniper.contrail.vro.config.getNetworkPolicyRules
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.ActionCall
import net.juniper.contrail.vro.workflows.model.ConditionAlternative
import net.juniper.contrail.vro.workflows.model.ConditionConjunction
import net.juniper.contrail.vro.workflows.model.FromBooleanParameter
import net.juniper.contrail.vro.workflows.model.FromListPropertyValue
import net.juniper.contrail.vro.workflows.model.FromStringParameter
import net.juniper.contrail.vro.workflows.model.WhenNonNull
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.simpleTypeQualifiers
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

val sourceAddressTypeParameterName = "srcAddressType"
val destinationAddressTypeParameterName = "dstAddressType"
val mirrorShowParameterName = "mirror"
val mirrorTypeParameterName = "mirrorType"
// There is no information about protocols in the schema
val defaultPort = "any"
val defaultProtocol = "any"
val allowedProtocols = listOf("any", "tcp", "udp", "icmp", "icmp6")
val defaultAddressType = "CIDR"
val allowedAddressTypes = listOf("CIDR", "Network", "Policy", "Security Group")
val defaultDirection = "<>"
val allowedDirections = listOf("<>", ">")
val defaultMirrorType = "Analyzer Instance"
val allowedMirrorTypes = listOf("Analyzer Instance", "NIC Assisted", "Analyzer IP")
val defaultJuniperHeaderOption = "enabled"
val allowedJuniperHeaderOptions = listOf("enabled", "disabled")
val defaultNexthopMode = "dynamic"
val allowedNexthopModes = listOf("dynamic", "static")

internal fun addRuleToPolicyWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add rule to network policy"

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
                additionalQualifiers += schema.simpleTypeQualifiers<ActionListType>("simpleAction")
            }
            parameter("protocol", string) {
                extractPropertyDescription<PolicyRuleType>(schema)
                mandatory = true
                defaultValue = defaultProtocol
                predefinedAnswers = allowedProtocols
            }
            parameter("direction", string) {
                // direction has no description in the schema
                description = "Direction"
                mandatory = true
                defaultValue = defaultDirection
                predefinedAnswers = allowedDirections
            }
        }
        step("Addresses") {
            visibility = WhenNonNull("parent")
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
                defaultValue = defaultPort
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

            parameter(mirrorShowParameterName, boolean) {
                description = "Mirror"
                mandatory = true
                defaultValue = false
            }
            parameter(mirrorTypeParameterName, string) {
                description = "Mirror Type"
                mandatory = true
                visibility = FromBooleanParameter(mirrorShowParameterName)
                defaultValue = defaultMirrorType
                predefinedAnswers = allowedMirrorTypes
            }
            parameter("analyzerInstance", reference<ServiceInstance>()) {
                description = "Analyzer Instance"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer Instance"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("analyzerName", string) {
                description = "Analyzer Name"
                mandatory = true
                visibility = ConditionConjunction(
                    ConditionAlternative(
                        FromStringParameter(mirrorTypeParameterName, "NIC Assisted"),
                        FromStringParameter(mirrorTypeParameterName, "Analyzer IP")),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("nicAssistedVlan", number) {
                description = "NIC Assisted VLAN"
                mandatory = true
                min = 1
                max = 4094
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "NIC Assisted"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("analyzerIP", string) {
                description = "Analyzer IP"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("analyzerMac", string) {
                description = "Analyzer MAC"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("udpPort", number) {
                description = "UDP Port"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("juniperHeader", string) {
                description = "Juniper Header"
                mandatory = true
                defaultValue = defaultJuniperHeaderOption
                predefinedAnswers = allowedJuniperHeaderOptions
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("nexthopMode", string) {
                description = "Nexthop Mode"
                mandatory = true
                defaultValue = defaultNexthopMode
                predefinedAnswers = allowedNexthopModes
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
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
                predefinedAnswersAction = ActionCall(
                    getNetworkPolicyRules,
                    "parent"
                )
            }
        }
        step("Basic Attributes") {
            visibility = WhenNonNull("rule")
            parameter("simpleAction", string) {
                extractPropertyDescription<ActionListType>(schema)
                additionalQualifiers += schema.simpleTypeQualifiers<ActionListType>("simpleAction")
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
                defaultValue = defaultProtocol
                predefinedAnswers = allowedProtocols
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
                predefinedAnswers = allowedDirections
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
                defaultValue = defaultAddressType
                predefinedAnswers = allowedAddressTypes
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
                defaultValue = defaultPort
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

            parameter(mirrorShowParameterName, boolean) {
                description = "Mirror"
                mandatory = true
                defaultValue = false
            }
            parameter(mirrorTypeParameterName, string) {
                description = "Mirror Type"
                mandatory = true
                visibility = FromBooleanParameter(mirrorShowParameterName)
                defaultValue = defaultMirrorType
                predefinedAnswers = allowedMirrorTypes
            }
            parameter("analyzerInstance", reference<ServiceInstance>()) {
                description = "Analyzer Instance"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer Instance"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("analyzerName", string) {
                description = "Analyzer Name"
                mandatory = true
                visibility = ConditionConjunction(
                    ConditionAlternative(
                        FromStringParameter(mirrorTypeParameterName, "NIC Assisted"),
                        FromStringParameter(mirrorTypeParameterName, "Analyzer IP")),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("nicAssistedVlan", number) {
                description = "NIC Assisted VLAN"
                mandatory = true
                min = 1
                max = 4094
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "NIC Assisted"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("analyzerIP", string) {
                description = "Analyzer IP"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("analyzerMac", string) {
                description = "Analyzer MAC"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("udpPort", number) {
                description = "UDP Port"
                mandatory = true
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("juniperHeader", string) {
                description = "Juniper Header"
                mandatory = true
                defaultValue = defaultJuniperHeaderOption
                predefinedAnswers = allowedJuniperHeaderOptions
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
            }
            parameter("nexthopMode", string) {
                description = "Nexthop Mode"
                mandatory = true
                defaultValue = defaultNexthopMode
                predefinedAnswers = allowedNexthopModes
                visibility = ConditionConjunction(
                    FromStringParameter(mirrorTypeParameterName, "Analyzer IP"),
                    FromBooleanParameter(mirrorShowParameterName)
                )
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