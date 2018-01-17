/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val String.isModelClassName get() = when (this) {
    "Project",
    "VirtualNetwork",
    "NetworkIpam",
    "FloatingIp",
    "FloatingIpPool",
    "NetworkPolicy",
    "SecurityGroup" -> true
    else -> false
}

val inventoryProperties = setOf(
    "QuotaType"
)

val nonEssentialAttributes = setOf(
    "VirtualNetworkPolicyType"
)

val String.isInventoryPropertyClassName get() =
    inventoryProperties.contains(this)

val String.isRequiredAttribute get() =
    ! nonEssentialAttributes.contains(this)

val Class<*>.isRequiredAttributeClass get() =
    simpleName.isRequiredAttribute

val ObjectClass.isModelClass get() =
    simpleName.isModelClassName

val Class<*>.isInventoryProperty get() =
    simpleName.isInventoryPropertyClassName

val ObjectClass.isRootClass: Boolean get() {
    val parentType = newInstance().defaultParentType

    if (parentType == null) return false
    if (parentType == "config-root") return true

    return ! parentType.typeToClassName.isModelClassName
}

val ObjectClass.isRelateable: Boolean get() =
    newInstance().defaultParent != null

val inventoryPropertyFilter: PropertyClassFilter = { it.isInventoryProperty }
val modelClassFilter: ObjectClassFilter = { it.isModelClass }
val rootClassFilter: ObjectClassFilter = { it.isRootClass }
