/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports

val hiddenProperties = setOf(
    "parentUuid",
    "parentType",
    "idPerms",
    "annotations"
)

val hiddenMethods = setOf(
    "setParent",
    "getObjectType",
    "getDefaultParentType",
    "getDefaultParent",
    "setDisplayName"
)

val String.isHiddenProperty get() =
    hiddenProperties.contains(this)

val String.displayedName get() = when (this) {
    "ipamSubnetMethod" -> "Subnet Method"
    "ipamSubnets" -> "Subnets"
    else -> camelChunks.joinToString(" ") { it.cleanOrRename.capitalize() }
}

val String.position get() =
    cleanedDisplayedProperty.propertyOrder

private val orderList = listOf(
    VirtualNetwork::class.java,
    NetworkIpam::class.java,
    FloatingIpPool::class.java,
    VirtualMachineInterface::class.java,

    NetworkPolicy::class.java,
    SecurityGroup::class.java,

    ApplicationPolicySet::class.java,
    FirewallPolicy::class.java,
    FirewallRule::class.java,
    ServiceGroup::class.java,
    AddressGroup::class.java,

    Project::class.java,
    PolicyManagement::class.java,
    Tag::class.java,
    ServiceTemplate::class.java,

    ServiceInstance::class.java,
    PortTuple::class.java,
    ServiceHealthCheck::class.java
)

private val orders = orderList.asSequence()
    .mapIndexed { index, clazz -> Pair(clazz, index) }
    .associateBy({ it.first }, { it.second })

val Class<*>.order get() =
    orders[this] ?: orders.size

private val String.propertyOrder get() = when (this) {
    "name" -> 0
    "displayName" -> 1
    "uuid" -> 2
    "perms2" -> 100
    "idPerms" -> 101
    else -> 99
}

val String.cleanedDisplayedProperty get() =
    replace(displayedPropertyPattern, "")

val String.cleanedRefProperty get() =
    replace(displayedRefsPropertyPattern, "")

val String.isDisplayOnlyProperty get() =
    endsWith(displayedPropertySuffix)

val String.isRefWrapperProperty get() =
    startsWith(refsPropertyPrefix)

val String.refWrapperPropertyDisplayName get() =
    "$displayedRefsPropertyPrefix ${cleanedRefProperty.folderName()}"

const val displayedPropertySuffix = "View"
val displayedPropertyPattern = "$displayedPropertySuffix$".toRegex()

val refsPropertyPrefix = "associated"
val refsPropertySuffix = "s"
val displayedRefsPropertyPrefix = refsPropertyPrefix.capitalize()
val displayedRefsPropertyPattern = "^$refsPropertyPrefix|$refsPropertySuffix$".toRegex()
