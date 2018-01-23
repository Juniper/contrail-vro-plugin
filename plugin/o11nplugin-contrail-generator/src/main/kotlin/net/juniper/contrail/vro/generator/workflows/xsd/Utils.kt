/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.xsd

import net.juniper.contrail.vro.config.camelChunks

private val String.handleShortcuts get() = when (this) {
    "dns" -> "DNS"
    else -> this
}

private val String.xsdChunk get() =
    toLowerCase().handleShortcuts

val String.xsdName get() =
    camelChunks.joinToString(separator = "-") { it.xsdChunk }

val Class<*>.xsdName get() =
    simpleName.xsdName

val String.stripSmi get() =
    if (startsWith("smi:")) substring(4) else this
