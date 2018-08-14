/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.referencePatterns
import net.juniper.contrail.vro.config.returnsObjectReferences
import net.juniper.contrail.vro.config.toPluginMethodName
import java.lang.reflect.Method

class CustomReference(val className: String, val methodName: String) {
    val pluginMethodName = methodName.toPluginMethodName
}

fun Method.toCustomReference(config: Config): CustomReference? {
    if ( ! returnsObjectReferences) return null

    val className = referencePatterns
        .map { it.matchEntire(name) }.filterNotNull()
        .map { it.groupValues[1] }
        .filter { config.isModelClassName(it) }
        .firstOrNull() ?: return null

    return CustomReference(className, name)
}