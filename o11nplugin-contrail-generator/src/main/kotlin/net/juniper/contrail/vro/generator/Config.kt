/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.generator.model.ObjectClassFilter
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

val Class<out ApiObjectBase>.isModelClass get() =
    simpleName.isModelClassName

val Class<out ApiPropertyBase>.isInventoryProperty get() =
    simpleName.isInventoryPropertyClassName

val Class<out ApiObjectBase>.isRootClass: Boolean get() {
    val parentType = defaultParentType
    if (parentType == null || parentType == "config-root") return true

    return ! parentType.typeToClassName.isModelClassName
}

val inventoryPropertyFilter: PropertyClassFilter = { it.isInventoryProperty }
val modelClassFilter: ObjectClassFilter = { it.isModelClass }
val rootClassFilter: ObjectClassFilter = { it.isRootClass }
