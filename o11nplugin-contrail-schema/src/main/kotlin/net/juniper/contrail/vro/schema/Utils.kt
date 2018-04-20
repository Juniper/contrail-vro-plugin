/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

import net.juniper.contrail.vro.config.camelChunks

private val String.handleShortcuts get() = when (this) {
    "dns" -> "DNS"
    else -> this
}

private val String.xsdChunk get() =
    toLowerCase().handleShortcuts

val String.xsdName get() =
    endNumberWithDash.replace("_", "-").camelChunks.joinToString(separator = "-") { it.xsdChunk }

private val endWithNumberPattern = "(\\w+)(\\d+)".toRegex()

val String.endNumberWithDash get() =
    endWithNumberPattern.matchEntire(this)?.groupValues?.let { "${it[1]}-${it[2]}" } ?: this

fun String.maybeToXsd(convert: Boolean) =
    if (convert) xsdName else this

val Class<*>.xsdName get() =
    simpleName.xsdName

val String.stripSmi get() =
    if (startsWith("smi:")) substring(4) else this
