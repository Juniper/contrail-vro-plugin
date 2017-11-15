/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.ConfigRoot

val apiPackageName = "net.juniper.contrail.api"
val generatedPackageName = "net.juniper.contrail.vro.generated"

fun propertyClasses() =
    ApiPropertyBase::class.java.nonAbstractSubclassesIn(apiPackageName)

fun objectClasses() =
    ApiObjectBase::class.java.nonAbstractSubclassesIn(apiPackageName)

fun rootClasses(classes: List<Class<out ApiObjectBase>>) =
        classes.filter { it.isRootClass }

val Class<out ApiObjectBase>.isRootClass: Boolean
    get() {
        val parentType = defaultParentType
        if (!isRelatable) return false
        return parentType == null
            || parentType == "config-root"
    }

val Class<out ApiObjectBase>.isRelatable: Boolean
    get() = this != ConfigRoot::class.java

fun String.dashedToCamelCase(): String =
    split("-").joinToString("") { it.toLowerCase().capitalize() }

fun String.splitCamel(): String {
    val sb = StringBuilder()
    for (i in 0 until length) {
        val c = this[i]
        if (i > 0 && c.isUpperCase()) {
            val nextI = i + 1
            if (nextI < length) {
                val nextC = this[nextI]
                if (!nextC.isUpperCase())
                    sb.append(" ")
            }
        }
        sb.append(c)
    }
    return sb.toString()
}

fun String.packageToPath() =
    replace('.', '/')
