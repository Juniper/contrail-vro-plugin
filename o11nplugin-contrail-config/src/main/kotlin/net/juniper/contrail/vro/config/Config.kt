/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import java.lang.reflect.Method

open class ConfigContext(
    val modelClasses : Set<String>,
    val inventoryProperties : Set<String>,
    val customPropertyObjects : Set<String>,
    val nonEssentialAttributes : Set<String>,
    val ignoredInWorkflows : Set<String>,
    val nonEditableProperties : Set<String>,
    val customPropertyValidation : Map<String, String>,
    val customCreateWorkflows : Set<String>,
    val customEditWorkflows : Set<String>,
    val customDeleteWorkflows : Set<String>,
    val directChildren : Set<String>,
    val mandatoryReference : Set<Pair<String, String>>,
    val nonEditableReference : Set<Pair<String, String>>,
    val customAddReference : Set<Pair<String, String>>,
    val customRemoveReference : Set<Pair<String, String>>,
    val hiddenRoots : Set<String>,
    val hiddenRelations : Set<Pair<String, String>>,
    val tagRelations : Set<String>,
    val relationAsProperty : Set<Pair<String, String>>,
    val reversedRelations : Set<Pair<String, String>>,
    val readUponQuery : Set<String>,
    val validateSecurityScope : Set<String>
)

open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}

class Config private constructor(val context: ConfigContext)
{

    companion object : SingletonHolder<Config, ConfigContext> (::Config)

    fun isModelClassName(str : String) =
        context.modelClasses.contains(str)

    fun isInventoryPropertyClassName(str : String) =
        context.inventoryProperties.contains(str)

    fun customValidationAction(str : String?) =
        context.customPropertyValidation[str]

    fun isRequiredAttribute(str : String) =
        ! context.nonEssentialAttributes.contains(str)

    fun isIgnoredInWorkflow(str : String) =
        context.ignoredInWorkflows.contains(str)

    fun isEditableProperty(str : String) =
        ! context.nonEditableProperties.contains(str)

    fun hasCustomCreateWorkflow(str : String) =
        context.customCreateWorkflows.contains(str)

    fun hasCustomEditWorkflow(str : String) =
        context.customEditWorkflows.contains(str)

    fun hasCustomDeleteWorkflow(str : String) =
        context.customDeleteWorkflows.contains(str)

    fun isDirectChild(str : String) =
        context.directChildren.contains(str)

    fun isHiddenRoot(str : String) =
        context.hiddenRoots.contains(str)

    fun isCustomPropertyObject(str : String) =
        context.customPropertyObjects.contains(str)

    fun notAHiddenTagParent(str : String, maybeTag: String) =
        if (maybeTag == "Tag") context.tagRelations.contains(str) else true

    fun isRelationMandatory(parent : ObjectClass, child: ObjectClass) =
        context.mandatoryReference.containsUnordered(parent.simpleName, child.simpleName)

    fun isRelationEditable(parent : ObjectClass, child: ObjectClass) =
        ! isInternal(parent) &&
            ! isInternal(child) &&
            notAHiddenTagParent(parent, child) &&
            ! context.nonEditableReference.containsUnordered(parent.simpleName, child.simpleName)

    fun isInPropertyRelationTo(parent : Class<*>, child: Class<*>) =
        context.relationAsProperty.contains(parent.simpleName, child.simpleName)

    fun notAHiddenTagParent(parent : ObjectClass, maybeTag: ObjectClass) =
        notAHiddenTagParent(parent.simpleName, maybeTag.simpleName)

    fun hasCustomAddReferenceWorkflow(parent : ObjectClass, child: Class<*>) =
        context.customAddReference.containsUnordered(parent.simpleName, child.simpleName)

    fun hasCustomRemoveReferenceWorkflow(parent : ObjectClass, child: Class<*>) =
        context.customRemoveReference.containsUnordered(parent.simpleName, child.simpleName)

    fun needsSecurityScopeValidation(objectClass : ObjectClass) =
        context.validateSecurityScope.contains(objectClass.simpleName)

    fun isDisplayableChildOf(child : String, parent: String) =
        child != parent &&
            ! context.hiddenRelations.containsUnordered(parent, child) &&
            notAHiddenTagParent(parent, child)

    fun isInReversedRelationTo(parent : String, child: String) =
        context.reversedRelations.containsUnordered(parent, child)

    fun isInReversedRelationTo(parent : Class<*>, child: Class<*>) =
        isInReversedRelationTo(parent.simpleName, child.simpleName)

    fun isPluginClass(clazz : Class<*>) =
        isModelClass(clazz) || clazz.isDefaultRoot

    fun isModelClass(clazz : Class<*>?) : Boolean {
        if (clazz == null) return false
        return isModelClassName(clazz.simpleName)
    }

    fun isInventoryProperty(clazz : Class<*>) =
        isInventoryPropertyClassName(clazz.simpleName)

    fun ignoredInWorkflow(clazz : Class<*>) =
        isIgnoredInWorkflow(clazz.simpleName)

    fun hasCustomCreateWorkflow(clazz : Class<*>) =
        hasCustomCreateWorkflow(clazz.simpleName)

    fun hasCustomEditWorkflow(clazz : Class<*>) =
        hasCustomEditWorkflow(clazz.simpleName)

    fun hasCustomDeleteWorkflow(clazz : Class<*>) =
        hasCustomDeleteWorkflow(clazz.simpleName)

    fun isDirectChild(clazz : Class<*>) =
        isDirectChild(clazz.simpleName)

    fun isDisplayableChildOf(child : Class<*>, parent: Class<*>) =
        isDisplayableChildOf(child.simpleName, parent.simpleName)

    fun isHiddenRoot(clazz : Class<*>) =
        isHiddenRoot(clazz.simpleName)

    fun isNodeClass(clazz : Class<*>) =
        clazz.isApiObjectClass || isInventoryProperty(clazz) || isCustomPropertyObject(clazz)

    fun isCustomPropertyObject(clazz : Class<*>) =
        isCustomPropertyObject(clazz.simpleName)

    fun isInternal(objectClass : ObjectClass) =
        !hasParents(objectClass)

    fun hasParents(objectClass : ObjectClass) =
        numberOfParents(objectClass) > 0

    fun hasParentsInModel(objectClass : ObjectClass) =
        numberOfParentsInModel(objectClass) > 0

    fun hasMultipleParents(objectClass : ObjectClass) =
        numberOfParents(objectClass) > 1

    fun hasMultipleParentsInModel(objectClass : ObjectClass) =
        numberOfParentsInModel(objectClass) > 1

    fun hasRootParent(objectClass : ObjectClass) =
        parents(objectClass).any { it.isDefaultRoot }

    fun numberOfParents(objectClass : ObjectClass) =
        parents(objectClass).count()

    fun numberOfParentsInModel(objectClass : ObjectClass) =
        parentsInModel(objectClass).count()

    fun parentsInModel(clazz : Class<*>) =
        parents(clazz).filter { isModelClass(it) }

    fun parentsInPlugin(clazz : Class<*>) =
        parents(clazz).filter { isPluginClass(it) }

    fun setParentMethods(clazz : Class<*>) =
        clazz.declaredMethods.asSequence()
            .filter { it.name == "setParent" }
            .filter { it.parameterCount == 1 }
            .filter { it.parameters[0].type.superclass == ApiObjectBase::class.java }

    fun parents(clazz : Class<*>) =
        setParentMethods(clazz).map { it.parameters[0].type as ObjectClass }
            .filter { isDisplayableChildOf(clazz, it) }

    fun isRootClass(objectClass : ObjectClass) : Boolean {
        if (isInternal(objectClass) || isHiddenRoot(objectClass) || objectClass.isDefaultRoot) return false

        val childOfRoot = parents(objectClass).any { it.isDefaultRoot }
        if (childOfRoot) return true

        val parentType = objectClass.defaultParentType ?: return false

        return ! isModelClassName(parentType.typeToClassName)
    }

}

object DefaultConfig : ConfigContext (
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
        the<AddressGroup>()
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
        the<FirewallRule>()
    ),

    customEditWorkflows = setOf(
        the<VirtualMachineInterface>(),
        the<NetworkPolicy>(),
        the<FloatingIp>(),
        the<ServiceTemplate>(),
        the<ServiceInstance>(),
        the<PortTuple>(),
        the<PolicyManagement>(),
        the<FirewallRule>()
    ),

    customDeleteWorkflows = setOf(
        the<VirtualMachineInterface>(),
        the<PortTuple>(),
        the<PolicyManagement>(),
        the<TagType>()
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
        the<VirtualMachineInterface>()
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
        pair<FirewallRule, VirtualNetwork>()
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
    )
)

fun getDefaultConfig() =
    Config.getInstance(DefaultConfig)

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
