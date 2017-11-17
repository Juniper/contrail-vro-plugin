/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.ConfigRoot

fun propertyClasses() =
    ApiPropertyBase::class.java.nonAbstractSubclasses()

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclasses()

fun rootClasses(classes: List<Class<out ApiObjectBase>>) =
    classes.filter { it.isRootClass }

val Class<out ApiObjectBase>.isRootClass: Boolean get() {
    if (!isRelatable) return false
    val parentType = defaultParentType
    return parentType == null
        || parentType == "config-root"
}

val Class<out ApiObjectBase>.isRelatable: Boolean get() =
    this != ConfigRoot::class.java

val Class<out ApiObjectBase>.defaultParentType: String? get() =
    newInstance().defaultParentType

val Class<out ApiObjectBase>.objectType: String get() =
    newInstance().objectType