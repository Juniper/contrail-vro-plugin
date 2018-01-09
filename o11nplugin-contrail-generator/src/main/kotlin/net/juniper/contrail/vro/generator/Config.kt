/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.util.defaultParentType
import net.juniper.contrail.vro.generator.util.parentClassName
import net.juniper.contrail.vro.generator.util.typeToClassName

val String.isModelClassName get() = when (this) {
    "Project",
    "VirtualNetwork",
    "NetworkIpam",
    "NetworkPolicy",
    "SecurityGroup" -> true
    else -> false
}

val Class<out ApiObjectBase>.isModelClass get() =
    simpleName.isModelClassName

val Class<out ApiObjectBase>.isParentModelClass get() =
    parentClassName?.isModelClassName ?: false

val Class<out ApiObjectBase>.isRootClass: Boolean get() {
    val parentType = defaultParentType
    if (parentType == null || parentType == "config-root") return true

    return ! parentType.typeToClassName.isModelClassName
}
