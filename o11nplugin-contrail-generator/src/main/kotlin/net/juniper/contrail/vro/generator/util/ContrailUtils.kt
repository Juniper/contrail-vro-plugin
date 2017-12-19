/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.util

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.ConfigRoot
import net.juniper.contrail.vro.generator.apiTypesPackageName

fun propertyClasses() =
    ApiPropertyBase::class.java.nonAbstractSubclasses()

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclasses()

fun List<Class<out ApiObjectBase>>.rootClasses() =
    filter { it.isRootClass }

val Class<*>.isApiTypeClass get() =
    `package` != null && `package`.name == apiTypesPackageName

val String.isApiTypeClass get() =
    classForName("$apiTypesPackageName.$this") != null

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

val Class<out ApiObjectBase>.parentClassName: String? get() =
    defaultParentType?.typeToClassName()
