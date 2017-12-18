/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

abstract class IdlComment(val comment: String) {
    lateinit var type: String
    lateinit var parentClassName: String
    lateinit var elementName: String
    lateinit var description: String
    var isRequired = false
    val insideQuotesRegex = "'[^']+'".toRegex()

    init {
        setProperties()
    }

    abstract fun setProperties()
}

class Link(comment: String) : IdlComment(comment) {
    lateinit var propertyClassName: String

    override fun setProperties() {
        val properties = insideQuotesRegex.findAll(comment).toList()

        if (properties.isEmpty()) throw IllegalArgumentException()

        type = "Link"
        elementName = properties[0].value
        propertyClassName = properties[1].value
        parentClassName = properties[2].value
        isRequired = checkIsRequired()
        description = properties.last().value
    }

    private fun checkIsRequired(): Boolean {
        val splitedString = comment.split("]")[1]

        if (!splitedString.matches(insideQuotesRegex)) return false

        val properties = insideQuotesRegex.findAll(splitedString).toList()
        return properties[0].value != "'optional'"
    }
}

class Property(comment: String) : IdlComment(comment) {

    override fun setProperties() {
        val properties = insideQuotesRegex.findAll(comment).toList()

        if (properties.isEmpty()) throw IllegalArgumentException()

        type = "Property"
        elementName = properties[0].value
        parentClassName = properties[1].value
        description = properties.last().value

        if (properties.size > 2) {
            val prop = properties[2].value
            isRequired = properties[2].value != "'optional'" // system-only is also required
        }
    }
}

fun extractType(comment: String): String =
    comment
        .replace("#IFMAP-SEMANTICS-IDL", "")
        .trim()
        .split("(")
        .first()
