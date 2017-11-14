/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.ConfigRoot

val packageName = "net.juniper.contrail.api"

fun propertyClasses() =
    ApiPropertyBase::class.java.nonAbstractSubclassesIn(packageName)

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclassesIn(packageName)


val Class<out ApiObjectBase>.isRootClass: Boolean get() {
    val parentType = defaultParentType
    if( ! isRelateable) return false
    return parentType == null
        || parentType == "config-root"
}

val Class<out ApiObjectBase>.isRelateable: Boolean get() =
    this != ConfigRoot::class.java

fun rootClasses() =
    objectClasses().filter { it.isRootClass }


