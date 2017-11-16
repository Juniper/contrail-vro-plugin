/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import java.nio.file.Path
import java.nio.file.Paths

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

fun Path.append(subpath: String): Path =
    Paths.get(toString(), subpath)

operator fun String.div(subpath: String): Path =
    Paths.get(this, subpath)

