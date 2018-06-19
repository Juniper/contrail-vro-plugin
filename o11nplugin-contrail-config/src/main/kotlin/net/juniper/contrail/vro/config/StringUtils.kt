/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import java.nio.file.Path
import java.nio.file.Paths

val whitespacesPattern = "\\s+".toRegex()

fun String.removeWhitespaces() =
    replace(whitespacesPattern, "")

val String.typeToClassName: String get() =
    split("-").toClassName()

val String.fieldToClassName: String get() =
    split("_").toClassName()

val String.classToTypeName: String get() =
    camelChunks.joinToString("-") { it.toLowerCase() }

private fun List<String>.toClassName() =
    joinToString("") { it.toLowerCase().capitalize() }

val String.isCapitalized get() =
    if (isNullOrBlank()) false else get(0).isUpperCase()

fun String.underscoredPropertyToCamelCase(): String {
    val elements = split("_")
    val first = elements[0]
    val rest = elements.slice(1 until elements.size)
    return first + rest.joinToString("") { it.toLowerCase().capitalize() }
}

fun String.toTitle(): String =
    splitCamel().capitalize()

fun String.splitCamel(): String =
    camelChunks.joinToString(" ")

val String.allLowerCase get() =
    removeTypeSuffix().camelChunks.joinToString(" ") { it.toAcronymOrLowercase() }

val Class<*>.allLowerCase get() =
    pluginName.allLowerCase

val String.allCapitalized get() =
    removeTypeSuffix().camelChunks.joinToString(" ") { it.cleanAcronym.capitalize() }

val Class<*>.allCapitalized get() =
    pluginName.allCapitalized

inline fun <reified T> descriptionOf() =
    T::class.java.allCapitalized

val typeSuffix = "Type$".toRegex()

fun String.removeTypeSuffix() =
    replace(typeSuffix, "")

val camelCasePattern = "(?<=[a-z0-9])(?=[A-Z])".toRegex()

val String.camelChunks get() =
    split(camelCasePattern)

fun String.folderName() : String = when (this) {
    "BgpAsAService" -> "BGPs As Services"
    "Bgpvpn" -> "BGP VPNs"
    "VirtualDns" -> "Virtual DNSes"
    else -> pluralizeCamelCases(this)
}

val Class<*>.folderName get() =
    pluginName.folderName()

val String.parameterName get() =
    decapitalize()

val Class<*>.parameterName get() =
    pluginName.parameterName

val String.pluralParameterName get(): String =
    folderName().replace(" ", "").run {
        if (startsWith("BGP")) this else decapitalize()
    }

val Class<*>.pluralParameterName get() =
    pluginName.pluralParameterName

fun pluralizeCamelCases(name: String) : String {
    val nameParts = name.split(camelCasePattern)
    val uppercasedWords = nameParts.map { it.cleanOrRename }.run {
        if (last() == "List") dropLast(1) else this
    }
    val pluralWord = uppercasedWords.last().pluralize()
    return (uppercasedWords.dropLast(1).plus(pluralWord)).joinToString(" ")
}

fun String.toAcronymOrLowercase(): String {
    val cleaned = cleanAcronym
    return if (cleaned == this) toLowerCase() else cleaned
}

val String.cleanAcronym get() = when (this) {
    "Uuid", "uuid" -> "UUID"
    "Ip", "ip" -> "IP"
    "Id", "id" -> "ID"
    "Ipam", "ipam" -> "IPAM"
    "Cidr", "cidr" -> "CIDR"
    "Dns", "dns" -> "DNS"
    "Bgp", "bgp" -> "BGP"
    "Vpn", "vpn" -> "VPN"
    "Pbb", "pbb" -> "PBB"
    "Evpn", "evpn" -> "EVPN"
    "Rpf", "rpf" -> "RPF"
    "Vxlan", "vxlan" -> "VxLAN"
    "Qos", "qos" -> "QoS"
    "Ecmp", "ecmp" -> "ECMP"
    "Dscp", "dscp" -> "DSCP"
    else -> this
}

val String.rename get() = when (this) {
    "Src" -> "Source"
    "Dst" -> "Destination"
    "Entries", "entries" -> "Rules"
    "Mgmt", "mgmt" -> "Configuration"
    "Perms2", "perms2" -> "Permissions"
    else -> this
}

val String.cleanOrRename get() =
    cleanAcronym.rename

private val String.isAlreadyPlural get() = when (this) {
    "Pairs", "Details", "Fields", "Type2", "Subnets", "Pools", "Routes", "Rules",
    "Ports", "Annotations", "Addresses", "Entries", "Permissions" -> true
    else -> false
}

val esSuffixesPattern = ".*(s|x|z|ch|sh)$".toRegex()
val nonIesSuffixesPattern = "(ay|ey|iy|oy|uy)$".toRegex()

fun String.pluralize(): String = when {
    isAlreadyPlural -> this
    matches(esSuffixesPattern) -> this + "es"
    endsWith("y") && !matches(nonIesSuffixesPattern) -> dropLast(1) + "ies"
    endsWith("list", true) -> this
    else -> this + "s"
}

fun String.blankToNull() =
    if (isBlank()) null else this

fun String.packageToPath() =
    replace('.', '/')

fun Path.append(subpath: String): Path =
    Paths.get(toString(), subpath)

operator fun String.div(subpath: String): Path =
    Paths.get(this, subpath)

operator fun Path.div(subpath: String): Path =
    Paths.get(toFile().absolutePath, subpath)

val String.isCDATA get() =
    startsWith("<![CDATA[") && endsWith("]]>")

val String.CDATA get() = when {
    this.isBlank() -> this
    this.isCDATA -> this
    else -> "<![CDATA[$this]]>"
}

val String?.CDATA
    @JvmName("getCDATANullable")
    get() = when {
    this == null -> null
    else -> this.CDATA
}

val String.withoutCDATA get() =
    if (isCDATA) removePrefix("<![CDATA[").removeSuffix("]]>") else this

val String.bold get() =
    "<b>$this</b>"