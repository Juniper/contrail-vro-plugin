/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.ConfigRoot

val apiPackageName = "net.juniper.contrail.api.types"

fun propertyClasses() =
    ApiPropertyBase::class.java.nonAbstractSubclasses()

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclasses()

fun List<Class<out ApiObjectBase>>.rootClasses() =
    filter { it.isRootClass }

typealias AliasClasses = Multimap<String, Class<*>>

data class NestedClasses(val nonAliasClasses: List<Class<*>>, val aliasClasses: AliasClasses)

fun List<Class<out ApiPropertyBase>>.nestedClasses(): NestedClasses {
    val tmpMap = HashMultimap.create<String, Class<*>>(64, 2)
    allInnerClasses().forEach { tmpMap.put(it.simpleName, it) }
    val nonAlias = mutableListOf<Class<*>>()
    val alias: AliasClasses = HashMultimap.create<String, Class<*>>(4, 4)
    tmpMap.asMap().onEach { (key, value) ->
        if (value.size == 1) nonAlias.addAll(value)
        else value.onEach { alias.put(key, it) }
    }

    return NestedClasses(nonAlias, alias)
}

val Class<*>.isApiClass get() =
    `package` != null && `package`.name == apiPackageName

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
