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

val Class<*>.order get() = when (this) {

    Project::class.java -> 100
    PolicyManagement::class.java -> 110
    Tag::class.java -> 120
    ServiceTemplate::class.java -> 130

    VirtualNetwork::class.java -> 10
    NetworkIpam::class.java -> 15
    FloatingIpPool::class.java -> 20
    VirtualMachineInterface::class.java -> 25

    NetworkPolicy::class.java -> 40
    SecurityGroup::class.java -> 42

    ApplicationPolicySet::class.java -> 60
    FirewallPolicy::class.java -> 62
    FirewallRule::class.java -> 64
    ServiceGroup::class.java -> 66
    AddressGroup::class.java -> 68

    ServiceInstance::class.java -> 200
    PortTuple::class.java -> 210
    ServiceHealthCheck::class.java -> 220

    else -> 1000
}

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
