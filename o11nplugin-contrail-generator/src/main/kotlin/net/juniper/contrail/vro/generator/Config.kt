/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.generator.model.ObjectClass
import net.juniper.contrail.vro.generator.model.ObjectClassFilter
import net.juniper.contrail.vro.generator.model.PropertyClass
import net.juniper.contrail.vro.generator.model.PropertyClassFilter
import net.juniper.contrail.vro.generator.util.defaultParentType
import net.juniper.contrail.vro.generator.util.typeToClassName

val String.isModelClassName get() = when (this) {
    "Project",
    "VirtualNetwork",
    "NetworkIpam",
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
    val parentType = defaultParentType
    if (parentType == null || parentType == "config-root") return true

    return ! parentType.typeToClassName.isModelClassName
}

val inventoryPropertyFilter: PropertyClassFilter = { it.isInventoryProperty }
val modelClassFilter: ObjectClassFilter = { it.isModelClass }
val rootClassFilter: ObjectClassFilter = { it.isRootClass }
