/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val hiddenProperties = setOf(
    "parentUuid",
    "parentType",
    "idPerms",
    "ipamSubnets",
    "annotations"
)

val hiddenMethods = setOf(
    "getObjectType",
    "getDefaultParentType",
    "getDefaultParent",
    "setDisplayName"
)

val String.isHiddenProperty get() =
    hiddenProperties.contains(this)

val String.displayedName get() = when (this) {
    "ipamSubnetMethod" -> "Subnet Method"
    else -> camelChunks.joinToString(" ") { it.cleanOrRename.capitalize() }
}

val String.position get() =
    cleanedDisplayedProperty.propertyPosition

private val String.propertyPosition get() = when (this) {
    "name" -> 0
    "displayName" -> 1
    "uuid" -> 2
    "perms2" -> 100
    "idPerms" -> 101
    else -> 99
}

val String.cleanedDisplayedProperty get() =
    replace(displayedPropertyPattern, "")

val String.cleanedBackRefProperty get() =
    replace(displayedBackRefsPropertyPattern, "")

val String.isDisplayOnlyProperty get() =
    endsWith(displayedPropertySuffix)

val String.isBackRefWrapperProperty get() =
    startsWith(backRefsPropertyPrefix)

val String.backRefWrapperPropertyDisplayName get() =
    "$displayedBackRefsPropertyPrefix ${cleanedBackRefProperty.folderName()}"

const val displayedPropertySuffix = "View"
val displayedPropertyPattern = "$displayedPropertySuffix$".toRegex()

val backRefsPropertyPrefix = "associated"
val displayedBackRefsPropertyPrefix = backRefsPropertyPrefix.capitalize()
val displayedBackRefsPropertyPattern = "^$backRefsPropertyPrefix|s$".toRegex()
