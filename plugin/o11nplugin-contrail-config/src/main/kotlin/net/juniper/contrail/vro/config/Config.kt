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

val String.isInventoryPropertyClassName get() = when (this) {
    "IdPermsType",
    "PermType2",
    "KeyValuePairs" -> false
    else -> true
}

val ObjectClass.isModelClass get() =
    simpleName.isModelClassName

val PropertyClass.isInventoryProperty get() =
    simpleName.isInventoryPropertyClassName

val ObjectClass.isRootClass: Boolean get() {
    val parentType = newInstance().defaultParentType
    if (parentType == null || parentType == "config-root") return true
    if (simpleName == "Port") return true

    return ! parentType.typeToClassName.isModelClassName
}

val inventoryPropertyFilter: PropertyClassFilter = { it.isInventoryProperty }
val modelClassFilter: ObjectClassFilter = { it.isModelClass }
val rootClassFilter: ObjectClassFilter = { it.isRootClass }
