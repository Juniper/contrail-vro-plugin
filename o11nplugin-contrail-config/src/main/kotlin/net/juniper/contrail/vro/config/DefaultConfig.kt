/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.types.ActionListType
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.ConfigRoot
import net.juniper.contrail.api.types.Domain
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FirewallRuleEndpointType
import net.juniper.contrail.api.types.FirewallServiceType
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.api.types.GlobalSystemConfig
import net.juniper.contrail.api.types.GlobalVrouterConfig
import net.juniper.contrail.api.types.IdPermsType
import net.juniper.contrail.api.types.InstanceIp
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.KeyValuePairs
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.PermType2
import net.juniper.contrail.api.types.PolicyManagement
import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.QuotaType
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.SequenceType
import net.juniper.contrail.api.types.ServiceApplianceSet
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.ServiceHealthCheck
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.TagType
import net.juniper.contrail.api.types.VirtualMachine
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.VirtualNetworkPolicyType
import java.lang.reflect.Method

val defaultContext = ConfigContext(
    modelClasses = setOf(
        the<Project>(),
        the<VirtualNetwork>(),
        the<NetworkIpam>(),
        the<FloatingIp>(),
        the<FloatingIpPool>(),
        the<NetworkPolicy>(),
        the<SecurityGroup>(),
        the<VirtualMachineInterface>(),
        the<ServiceHealthCheck>(),
        the<VirtualMachine>(),
        the<ServiceInstance>(),
        the<ServiceTemplate>(),
        the<PortTuple>(),
        the<InstanceIp>(),
        the<PortTuple>(),
        the<PolicyManagement>(),
        the<Tag>(),
        the<TagType>(),
        the<ApplicationPolicySet>(),
        the<FirewallPolicy>(),
        the<FirewallRule>(),
        the<ServiceGroup>(),
        the<AddressGroup>(),
        the<GlobalSystemConfig>(),
        the<GlobalVrouterConfig>()
    ),

    inventoryProperties = setOf(
        the<QuotaType>()
    ),

    customPropertyObjects = setOf(
        the<IpamSubnetType>()
    ),

    nonEssentialAttributes = setOf(
        the<VirtualNetworkPolicyType>()
    ),

    ignoredInWorkflows = setOf(
        the<KeyValuePairs>(),
        the<PermType2>(),
        the<IdPermsType>(),
        the<SequenceType>(),
        the<ActionListType>(),
        the<FirewallServiceType>(),
        the<FirewallRuleEndpointType>()
    ),

    nonEditableProperties = setOf(
        "displayName",
        "parentType",
        "defaultParentType",
        "objectType",
        "networkId"
    ),

    customPropertyValidation = mapOf(
        "vxlanNetworkIdentifier" to isValidVxLANId
    ),

    customCreateWorkflows = setOf(
        the<VirtualMachineInterface>(),
        the<FloatingIp>(),
        the<ServiceTemplate>(),
        the<ServiceInstance>(),
        the<PortTuple>(),
        the<PolicyManagement>(),
        the<Tag>(),
        the<TagType>(),
        the<FirewallRule>(),
        the<GlobalSystemConfig>(),
        the<GlobalVrouterConfig>()
    ),

    customEditWorkflows = setOf(
        the<VirtualMachineInterface>(),
        the<NetworkPolicy>(),
        the<FloatingIp>(),
        the<ServiceTemplate>(),
        the<ServiceInstance>(),
        the<PortTuple>(),
        the<PolicyManagement>(),
        the<FirewallRule>(),
        the<GlobalSystemConfig>(),
        the<GlobalVrouterConfig>()
    ),

    customDeleteWorkflows = setOf(
        the<VirtualMachineInterface>(),
        the<PortTuple>(),
        the<PolicyManagement>(),
        the<TagType>(),
        the<GlobalSystemConfig>(),
        the<GlobalVrouterConfig>()
    ),

    directChildren = setOf(
        the<FloatingIp>()
    ),

    mandatoryReference = setOf(
        pair<VirtualMachineInterface, VirtualNetwork>(),
        pair<ServiceInstance, ServiceTemplate>()
    ),

    nonEditableReference = setOf(
        pair<VirtualMachineInterface, VirtualNetwork>(),
        pair<VirtualNetwork, NetworkIpam>(),
        pair<ServiceInstance, ServiceTemplate>()
    ),

    customAddReference = setOf(
        pair<FloatingIp, VirtualMachineInterface>(),
        pair<VirtualNetwork, NetworkPolicy>(),
        pair<ServiceHealthCheck, ServiceInstance>(),
        pair<VirtualNetwork, NetworkIpam>(),
        pair<FirewallPolicy, FirewallRule>(),
        pair<ApplicationPolicySet, FirewallPolicy>()
    ),

    customRemoveReference = setOf(
        pair<FloatingIp, VirtualMachineInterface>(),
        pair<VirtualNetwork, NetworkIpam>()
    ),

    hiddenRoots = setOf(
        the<VirtualMachineInterface>(),
        the<GlobalSystemConfig>()
    ),

    hiddenRelations = setOf(
        pair<FloatingIp, Project>(),
        pair<VirtualMachineInterface, PortTuple>(),
        pair<VirtualMachineInterface, VirtualMachine>(),
        pair<ServiceTemplate, ServiceApplianceSet>(),
        pair<Project, PolicyManagement>(),
        pair<ConfigRoot, PolicyManagement>(),
        pair<Tag, TagType>(),
        pair<FirewallRule, AddressGroup>(),
        pair<FirewallRule, ServiceGroup>(),
        pair<FirewallRule, VirtualNetwork>(),
        pair<ApplicationPolicySet, GlobalVrouterConfig>(),
        pair<GlobalSystemConfig, GlobalVrouterConfig>()
    ),

    tagRelations = setOf(
        the<ConfigRoot>(),
        the<Project>(),
        the<VirtualNetwork>(),
        the<VirtualMachineInterface>(),
        the<ApplicationPolicySet>()
    ),

    relationAsProperty = setOf(
        pair<VirtualMachineInterface, VirtualMachine>()
    ),

    reversedRelations = setOf(
        pair<FloatingIp, VirtualMachineInterface>()
    ),

    readUponQuery = setOf(
        the<ApplicationPolicySet>(),
        the<FirewallPolicy>(),
        the<FirewallRule>(),
        the<AddressGroup>(),
        the<ServiceGroup>(),
        the<Tag>()
    ),

    validateSecurityScope = setOf(
        the<ApplicationPolicySet>(),
        the<FirewallPolicy>(),
        the<FirewallRule>(),
        the<AddressGroup>(),
        the<ServiceGroup>(),
        the<Tag>()
    ),

    draftClasses = setOf(
        the<Project>()
    )
)

inline fun <reified T> the() =
    T::class.java.simpleName

inline fun <reified T1, reified T2> pair() =
    Pair(the<T1>(), the<T2>())

fun <T> Set<Pair<T, T>>.contains(first: T, second: T) =
    contains(Pair(first, second))

fun <T> Set<Pair<T, T>>.containsUnordered(first: T, second: T) =
    contains(Pair(first, second)) || contains(Pair(second, first))

val VirtualMachineInterfaceName = "VirtualMachineInterface"
val PortName = "Port"

val String.toPluginName get() = when (this) {
    // Virtual Machine Interface is visible in Contrail UI as Port
    VirtualMachineInterfaceName -> PortName
    else -> this
}

val String.referenceFolderPrefix get() = when (this) {
    "Tag", "ApplicationPolicySet" -> "Applied"
    else -> ""
}

val Class<*>.referenceFolderPrefix get() =
    this.simpleName.referenceFolderPrefix

val Class<*>.referenceFolderName get() =
    "$referenceFolderPrefix $folderName".trim()

val String.toPluginMethodName get() =
    this.replace(VirtualMachineInterfaceName, PortName)

val Method.pluginPropertyName get() =
    this.nameWithoutGet.toPluginMethodName.decapitalize()

val Class<*>.pluginName get() =
    this.simpleName.toPluginName

val Class<*>.isConfigRoot get() =
    isA<ConfigRoot>()

val Class<*>.isDomain get() =
    isA<Domain>()

val Class<*>.isPolicyManagement get() =
    isA<PolicyManagement>()

val Class<*>.isDefaultRoot get() =
    isConfigRoot || isDomain