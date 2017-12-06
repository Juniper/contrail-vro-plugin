/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import java.nio.file.Path
import java.nio.file.Paths

val CAMEL_CASE_REGEX = "(?<=[a-z])(?=[A-Z])".toRegex()
val ES_SUFFIXES = ".*(s|x|z|ch|sh)$".toRegex()
val NON_IES_SUFFIXES_REGEX = "(ay|ey|iy|oy|uy)\$".toRegex()

fun String.typeToClassName(): String =
    split("-").joinToString("") { it.toLowerCase().capitalize() }

fun String.underscoredPropertyToCamelCase(): String {
    val elements = split("_")
    val first = elements[0]
    val rest = elements.slice(1 until elements.size)
    return first + rest.joinToString("") { it.toLowerCase().capitalize() }
}

fun String.splitCamel(): String =
    split(CAMEL_CASE_REGEX).joinToString(" ")

fun String.folderName() : String = when (this) {
    "BgpAsAService" -> "BGPs As Services"
    "Bgpvpn" -> "BGP VPNs"
    "VirtualDns" -> "Virtual DNSes"
    else -> pluralizeCamelCases(this)
}

fun pluralizeCamelCases(name: String) : String {
    val splitted = name.split(CAMEL_CASE_REGEX)
    val uppercasedWords = splitted.map { uppercaseAcronyms(it) }
    val pluralWord = uppercasedWords.last().pluralize()
    return (uppercasedWords.dropLast(1).plus(pluralWord)).joinToString(" ")
}

fun uppercaseAcronyms(name: String): String = when (name) {
    "Ip", "ip" -> "IP"
    "Bgp", "bgp" -> "BGP"
    "Vpn", "vpn" -> "VPN"
    else -> name
}

fun String.pluralize(): String = when {
    matches(ES_SUFFIXES) -> this + "es"
    endsWith("y") && !matches(NON_IES_SUFFIXES_REGEX) -> dropLast(1) + "ies"
    endsWith("list", true) -> this
    else -> this + "s"
}

fun String.packageToPath() =
    replace('.', '/')

fun Path.append(subpath: String): Path =
    Paths.get(toString(), subpath)

operator fun String.div(subpath: String): Path =
    Paths.get(this, subpath)

val String?.CDATA get() =
    if (this == null) null else "<![CDATA[$this]]>"