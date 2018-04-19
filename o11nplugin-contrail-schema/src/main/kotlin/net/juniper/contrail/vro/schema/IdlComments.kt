/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

import net.juniper.contrail.vro.config.blankToNull

sealed class IdlComment {
    abstract val type: String
    abstract val parentClassName: String
    abstract val elementName: String
    abstract val description: String?
    abstract val crud: CRUD
    abstract val isRequired: Boolean
}

class Link(comment: String) : IdlComment() {
    override val type = idlLink
    override val parentClassName: String
    override val elementName: String
    override val description: String?
    override val crud: CRUD
    override val isRequired: Boolean
    val propertyClassName: String

    init {
        val properties = comment.commentProperties
        if (properties.size < 3)
            throw IllegalArgumentException("Link definition should have at least 3 elements.")
        elementName = properties[0]
        parentClassName = properties[1]
        propertyClassName = properties[2]
        isRequired = properties.getOrNull(4).isRequired
        crud = properties.getOrNull(5).toCrud()
        description = properties.getOrNull(6).cleanDescription
    }
}

class Property(comment: String) : IdlComment() {
    override val type = idlProperty
    override val parentClassName: String
    override val elementName: String
    override val description: String?
    override val crud: CRUD
    override val isRequired: Boolean

    init {
        val properties = comment.commentProperties
        if (properties.size < 2)
            throw IllegalArgumentException("Property definition should have at least 2 elements.")
        elementName = properties[0]
        parentClassName = properties[1]
        isRequired = properties.getOrNull(2).isRequired
        crud = properties.getOrNull(3).toCrud()
        description = properties.getOrNull(4).cleanDescription
    }
}

class ListProperty(comment: String) : IdlComment() {
    override val type: String get() = idlListProperty
    override val parentClassName: String
    override val elementName: String
    override val description: String?
    override val crud: CRUD
    override val isRequired: Boolean

    init {
        val properties = comment.commentProperties
        if (properties.size < 2)
            throw IllegalArgumentException("ListProperty definition should have at least 2 elements.")
        elementName = properties[0]
        parentClassName = properties[1]
        isRequired = properties.getOrNull(2).isRequired
        crud = properties.getOrNull(3).toCrud()
        description = properties.getOrNull(4).cleanDescription
    }
}

private val linkElementPattern = "(?s)\\[\\s*'(.+?)'\\s*]|'(.+?)'".toRegex()
private val arraySeparatorPattern = "'\\s*,\\s*'".toRegex()
private val idlTypePattern = "(?s)(\\w+)\\s*\\(.+\\)".toRegex()
val ifmapHeaderPattern = "\\s*$ifmapIdlName\\s+".toRegex()
val commentEntrySeparator = "\\s*;\\s*".toRegex()
val descriptionIndent = "\n\\s+".toRegex()

private val String.commentProperties get() =
    linkElementPattern.findAll(this)
        .map { it.linkElementValue() }
        .toList()

private fun MatchResult.linkElementValue(): String {
    val arrayValue = groups[1]?.value
    val regularValue = groups[2]?.value
    return arrayValue?.split(arraySeparatorPattern)?.joinToString("\n")
        ?: regularValue ?: ""
}

private val String?.isRequired get() =
    if (this == null) false else this != optional

private val String?.cleanDescription get() =
    if (this == null) this else replace(descriptionIndent, " ")

val String.commentType get() =
    idlTypePattern.find(this)?.groupValues?.get(1)

fun <T : IdlComment> Sequence<T>.withElementName(name: String) =
    filter { it.elementName == name }

fun <T : IdlComment> Sequence<T>.formatDescription() =
    map { it.description }.filterNotNull().joinToString("\n\n").blankToNull()
