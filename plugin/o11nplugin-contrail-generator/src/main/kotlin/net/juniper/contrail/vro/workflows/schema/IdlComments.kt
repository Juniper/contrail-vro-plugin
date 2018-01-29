/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.schema

private val insideQuotesRegex = "'[^']+'".toRegex()

private val String.commentProperties get() =
    insideQuotesRegex.findAll(this)
        .map { it.value }
        .map { it.replace("'", "") }
        .toList()

sealed class IdlComment(val comment: String) {
    lateinit var type: String
    lateinit var parentClassName: String
    lateinit var elementName: String
    lateinit var description: String
    var isRequired = false

    init {
        setProperties()
    }

    abstract fun setProperties()
}

class Link(comment: String) : IdlComment(comment) {
    lateinit var propertyClassName: String

    override fun setProperties() {
        val properties = comment.commentProperties

        if (properties.isEmpty()) throw IllegalArgumentException()

        type = "Link"
        elementName = properties[0]
        propertyClassName = properties[1]
        parentClassName = properties[2]
        isRequired = checkIsRequired()
        description = properties.last()
    }

    private fun checkIsRequired(): Boolean {
        val splitedString = comment.split("]")[1]

        if (!splitedString.matches(insideQuotesRegex)) return false

        val properties = insideQuotesRegex.findAll(splitedString).toList()
        return properties[0].value != optional
    }
}

class Property(comment: String) : IdlComment(comment) {

    override fun setProperties() {
        val properties = comment.commentProperties

        if (properties.isEmpty()) throw IllegalArgumentException()

        type = "Property"
        elementName = properties[0]
        parentClassName = properties[1]
        description = properties.last()

        if (properties.size > 2) {
            isRequired = properties[2] != optional // system-only is also required
        }
    }
}

fun String.extractCommentType(): String =
    replace(ifmapIdlName, "")
    .trim()
    .split("(")
    .first()
