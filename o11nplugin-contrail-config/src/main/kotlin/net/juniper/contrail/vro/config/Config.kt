/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import java.lang.reflect.Method

val modelClasses = setOf(
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
    the<AddressGroup>()
)

val inventoryProperties = setOf(
    the<QuotaType>()
)

val customPropertyObjects = setOf(
    the<IpamSubnetType>()
)

val nonEssentialAttributes = setOf(
    the<VirtualNetworkPolicyType>()
)

val ignoredInWorkflows = setOf(
    the<KeyValuePairs>(),
    the<PermType2>(),
    the<IdPermsType>(),
    the<SequenceType>(),
    the<ActionListType>(),
    the<FirewallServiceType>(),
    the<FirewallRuleEndpointType>()
)

val nonEditableProperties = setOf(
    "displayName",
    "parentType",
    "defaultParentType",
    "objectType",
    "networkId"
)

val customPropertyValidation = mapOf(
    "vxlanNetworkIdentifier" to isValidVxLANId
)

val customCreateWorkflows = setOf(
    the<VirtualMachineInterface>(),
    the<FloatingIp>(),
    the<ServiceTemplate>(),
    the<ServiceInstance>(),
    the<PortTuple>(),
    the<Tag>()
)

val customEditWorkflows = setOf(
    the<VirtualMachineInterface>(),
    the<NetworkPolicy>(),
    the<FloatingIp>(),
    the<ServiceTemplate>(),
    the<ServiceInstance>(),
    the<PortTuple>()
)

val customDeleteWorkflows = setOf(
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

val nonEditableReference = setOf(
    pair<VirtualMachineInterface, VirtualNetwork>(),
    pair<VirtualNetwork, NetworkIpam>(),
    pair<ServiceInstance, ServiceTemplate>()
)

val customAddReference = setOf(
    pair<FloatingIp, VirtualMachineInterface>(),
    pair<VirtualNetwork, NetworkPolicy>(),
    pair<ServiceHealthCheck, ServiceInstance>(),
    pair<VirtualNetwork, NetworkIpam>()
)

val customRemoveReference = setOf(
    pair<FloatingIp, VirtualMachineInterface>(),
    pair<VirtualNetwork, NetworkIpam>()
)

val hiddenRoots = setOf(
    the<VirtualMachineInterface>()
)

val hiddenRelations = setOf(
    pair<FloatingIp, Project>(),
    pair<VirtualMachineInterface, PortTuple>(),
    pair<VirtualMachineInterface, VirtualMachine>(),
    pair<ServiceTemplate, ServiceApplianceSet>(),
    pair<Tag, TagType>()
)

val tagRelations = setOf(
    the<Project>(),
    the<VirtualNetwork>(),
    the<VirtualMachineInterface>(),
    the<ApplicationPolicySet>()
)

val relationAsProperty = setOf(
    pair<VirtualMachineInterface, VirtualMachine>()
)

val reversedRelations = setOf(
    pair<FloatingIp, VirtualMachineInterface>()
)

private inline fun <reified T> the() =
    T::class.java.simpleName

private inline fun <reified T1, reified T2> pair() =
    Pair(the<T1>(), the<T2>())

val String.isModelClassName get() =
    modelClasses.contains(this)

val String.isInventoryPropertyClassName get() =
    inventoryProperties.contains(this)

val String?.customValidationAction get() =
    customPropertyValidation[this]

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

val String.isCustomPropertyObject get() =
    customPropertyObjects.contains(this)

infix fun String.notAHiddenTagParent(maybeTag: String) =
    if (maybeTag == "Tag") tagRelations.contains(this) else true

fun ObjectClass.isRelationMandatory(child: ObjectClass) =
    mandatoryReference.containsUnordered(simpleName, child.simpleName)

fun ObjectClass.isRelationEditable(child: ObjectClass) =
    ! isInternal &&
    ! child.isInternal &&
    this notAHiddenTagParent child &&
    ! nonEditableReference.containsUnordered(simpleName, child.simpleName)

fun Class<*>.isInPropertyRelationTo(child: Class<*>) =
    relationAsProperty.contains(simpleName, child.simpleName)

infix fun ObjectClass.notAHiddenTagParent(maybeTag: ObjectClass) =
    simpleName notAHiddenTagParent maybeTag.simpleName

private fun <T> Set<Pair<T, T>>.contains(first: T, second: T) =
    contains(Pair(first, second))

private fun <T> Set<Pair<T, T>>.containsUnordered(first: T, second: T) =
    contains(Pair(first, second)) || contains(Pair(second, first))

fun ObjectClass.hasCustomAddReferenceWorkflow(child: Class<*>) =
    customAddReference.containsUnordered(simpleName, child.simpleName)

fun ObjectClass.hasCustomRemoveReferenceWorkflow(child: Class<*>) =
    customRemoveReference.containsUnordered(simpleName, child.simpleName)

infix fun String.isDisplayableChildOf(parent: String) =
    this != parent &&
    ! hiddenRelations.containsUnordered(parent, this) &&
    parent notAHiddenTagParent this

fun String.isInReversedRelationTo(child: String) =
    reversedRelations.containsUnordered(this, child)

fun Class<*>.isInReversedRelationTo(child: Class<*>) =
    simpleName.isInReversedRelationTo(child.simpleName)

val Class<*>?.isConfigRoot get() =
    isA<ConfigRoot>()

val Class<*>?.isDomain get() =
    isA<Domain>()

val Class<*>?.isDefaultRoot get() =
    isConfigRoot || isDomain

val Class<*>?.isPluginClass get() =
    isModelClass || isDefaultRoot

val Class<*>?.isModelClass get() =
    this?.simpleName?.isModelClassName ?: false

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

infix fun Class<*>.isDisplayableChildOf(parent: Class<*>) =
    simpleName isDisplayableChildOf parent.simpleName

val Class<*>.isHiddenRoot get() =
    simpleName.isHiddenRoot

val Class<*>.isNodeClass get() =
    isApiObjectClass || isInventoryProperty || isCustomPropertyObject

val Class<*>.isCustomPropertyObject get() =
    simpleName.isCustomPropertyObject

val ObjectClass.isInternal get() =
    !hasParents

val ObjectClass.hasParents get() =
    numberOfParents > 0

val ObjectClass.hasParentsInModel get() =
    numberOfParentsInModel > 0

val ObjectClass.hasMultipleParents get() =
    numberOfParents > 1

val ObjectClass.hasMultipleParentsInModel get() =
    numberOfParentsInModel > 1

val ObjectClass.hasRootParent get() =
    parents.any { it.isDefaultRoot }

val ObjectClass.numberOfParents get() =
    parents.count()

val ObjectClass.numberOfParentsInModel get() =
    parentsInModel.count()

val Class<*>.parentsInModel get() =
    parents.filter { it.isModelClass }

val Class<*>.parentsInPlugin get() =
    parents.filter { it.isPluginClass }

val Class<*>.setParentMethods get() =
    declaredMethods.asSequence()
        .filter { it.name == "setParent" }
        .filter { it.parameterCount == 1 }
        .filter { it.parameters[0].type.superclass == ApiObjectBase::class.java }

val Class<*>.parents get() =
    setParentMethods.map { it.parameters[0].type as ObjectClass }

val ObjectClass.isRootClass: Boolean get() {
    if (isInternal || isHiddenRoot || isDefaultRoot) return false

    val childOfRoot = parents.any { it.isDefaultRoot }
    if (childOfRoot) return true

    val parentType = defaultParentType ?: return false

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

val Method.pluginPropertyName get() =
    nameWithoutGet.toPluginMethodName.decapitalize()

val Class<*>.pluginName get() =
    simpleName.toPluginName
