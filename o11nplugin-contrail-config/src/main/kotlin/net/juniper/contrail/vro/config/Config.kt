/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports

val modelClasses = setOf(
    the<Project>(),
    the<VirtualNetwork>(),
    the<Subnet>(),
    the<NetworkIpam>(),
    the<FloatingIp>(),
    the<FloatingIpPool>(),
    the<NetworkPolicy>(),
    the<SecurityGroup>(),
    the<VirtualMachineInterface>(),
    the<RouteTable>(),
    the<ServiceInstance>(),
    the<ServiceTemplate>(),
    the<PortTuple>(),
    the<InstanceIp>(),
    the<QosConfig>(),
    the<GlobalQosConfig>(),
    the<PortTuple>()
)

val inventoryProperties = setOf(
    the<QuotaType>()
)

val nonEssentialAttributes = setOf(
    the<VirtualNetworkPolicyType>()
)

val ignoredInWorkflows = setOf(
    the<KeyValuePairs>(),
    the<PermType2>(),
    the<IdPermsType>(),
    the<SequenceType>()
)

val nonEditableProperties = setOf(
    "displayName",
    "parentType",
    "defaultParentType",
    "objectType",
    "networkId"
)

val customCreateWorkflows = setOf(
    the<FloatingIp>()
)

val customEditWorkflows = setOf(
    the<NetworkPolicy>(),
    the<FloatingIp>(),
    the<RouteTable>()
)

val customDeleteWorkflows = setOf(
    the<FloatingIpPool>(),
    the<VirtualMachineInterface>(),
    the<PortTuple>()
)

val directChildren = setOf(
    the<FloatingIp>()
)

val mandatoryReference = setOf(
    pair<VirtualMachineInterface, VirtualNetwork>(),
    pair<ServiceInstance, ServiceTemplate>()
)

val hiddenRoots = setOf(
    the<VirtualMachineInterface>()
)

val hiddenRelations = setOf(
    pair<FloatingIp, Project>(),
    pair<VirtualNetwork, NetworkIpam>(),
    pair<VirtualMachineInterface, VirtualMachineInterface>(),
    pair<VirtualMachineInterface, PortTuple>()
)

private inline fun <reified T> the() =
    T::class.java.simpleName

private inline fun <reified T1, reified T2> pair() =
    Pair(the<T1>(), the<T2>())

val String.isModelClassName get() =
    modelClasses.contains(this)

val String.isInventoryPropertyClassName get() =
    inventoryProperties.contains(this)

val String.isRequiredAttribute get() =
    ! nonEssentialAttributes.contains(this)

val String.isIgnoredInWorkflow get() =
    ignoredInWorkflows.contains(this)

val String.isEditableProperty get() =
    ! nonEditableProperties.contains(this)

val String.hasCustomCreateWorkflow get() =
    customCreateWorkflows.contains(this)

val String.hasCustomEditWorkflow get() =
    customEditWorkflows.contains(this)

val String.hasCustomDeleteWorkflow get() =
    customDeleteWorkflows.contains(this)

val String.isDirectChild get() =
    directChildren.contains(this)

val String.isHiddenRoot get() =
    hiddenRoots.contains(this)

fun Class<*>.isRelationMandatory(child: Class<*>) =
    mandatoryReference.contains(Pair(simpleName, child.simpleName))

infix fun String.isDisplayableChildOf(parent: String) =
    ! hiddenRelations.contains(Pair(parent, this))

val Class<*>.isRequiredAttributeClass get() =
    simpleName.isRequiredAttribute

val ObjectClass.isModelClass get() =
    simpleName.isModelClassName

val Class<*>.isInventoryProperty get() =
    simpleName.isInventoryPropertyClassName

val Class<*>.ignoredInWorkflow get() =
    simpleName.isIgnoredInWorkflow

val Class<*>.hasCustomCreateWorkflow get() =
    simpleName.hasCustomCreateWorkflow

val Class<*>.hasCustomEditWorkflow get() =
    simpleName.hasCustomEditWorkflow

val Class<*>.hasCustomDeleteWorkflow get() =
    simpleName.hasCustomDeleteWorkflow

val Class<*>.isDirectChild get() =
    simpleName.isDirectChild

val Class<*>.isHiddenRoot get() =
    simpleName.isHiddenRoot

val ObjectClass.isRootClass: Boolean get() {
    val parentType = newInstance().defaultParentType

    if (parentType == null) return false
    if (parentType == "config-root") return true
    if (isHiddenRoot) return false

    return ! parentType.typeToClassName.isModelClassName
}

private val VirtualMachineInterfaceName = "VirtualMachineInterface"
private val PortName = "Port"

val String.toPluginName get() = when (this) {
    // Virtual Machine Interface is visible in Contrail UI as Port
    VirtualMachineInterfaceName -> PortName
    else -> this
}

val String.toPluginMethodName get() =
    replace(VirtualMachineInterfaceName, PortName)

val Class<*>.pluginName get() =
    simpleName.toPluginName

val inventoryPropertyFilter: PropertyClassFilter = { it.isInventoryProperty }
val modelClassFilter: ObjectClassFilter = { it.isModelClass }
val rootClassFilter: ObjectClassFilter = { it.isRootClass }
