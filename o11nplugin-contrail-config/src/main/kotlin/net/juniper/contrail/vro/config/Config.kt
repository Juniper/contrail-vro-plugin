/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val modelClasses = setOf(
    "Project",
    "VirtualNetwork",
    "Subnet",
    "NetworkIpam",
    "FloatingIp",
    "FloatingIpPool",
    "NetworkPolicy",
    "SecurityGroup",
    "VirtualMachineInterface",
    "RouteTable",
    "ServiceInstance",
    "ServiceTemplate",
    "PortTuple",
    "InstanceIp",
    "QosConfig",
    "GlobalQosConfig"
)

val inventoryProperties = setOf(
    "QuotaType"
)

val nonEssentialAttributes = setOf(
    "VirtualNetworkPolicyType"
)

val ignoredInWorkflows = setOf(
    "KeyValuePairs",
    "PermType2",
    "IdPermsType",
    "SequenceType"
)

val nonEditableProperties = setOf(
    "displayName",
    "parentType",
    "defaultParentType",
    "objectType",
    "networkId"
)

val customCreateWorkflows = setOf(
    "FloatingIp"
)

val customEditWorkflows = setOf(
    "NetworkPolicy",
    "FloatingIp",
    "RouteTable"
)

val customDeleteWorkflows = setOf(
    "FloatingIpPool",
    "VirtualMachineInterface"
)

val directChildren = setOf(
    "FloatingIp"
)

val mandatoryReference = setOf(
    Pair("VirtualMachineInterface", "VirtualNetwork"),
    Pair("ServiceInstance", "ServiceTemplate")
)

val hiddenRoots = setOf(
    "VirtualMachineInterface"
)

val hiddenRelations = setOf(
    Pair("FloatingIp", "Project"),
    Pair("VirtualNetwork", "NetworkIpam")
)

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

val ObjectClass.isInternal: Boolean get() =
    newInstance().defaultParentType == null

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
val internalClassFilter: ObjectClassFilter = { it.isInternal }
