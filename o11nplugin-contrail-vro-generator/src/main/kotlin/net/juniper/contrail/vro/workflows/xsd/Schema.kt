/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

import org.w3c.dom.Node
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory

val XSD_TYPES = arrayOf("xsd:complexType", "xsd:simpleType", "xsd:element")

private fun loadXSDSchemaFrom(path: Path): Node {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile())
    return document.lastChild
}

fun buildComment(comment: String): List<IdlComment> =
    comment.split(";").mapNotNull { buildCommentObject(it) }

private fun buildCommentObject(comment: String): IdlComment? =
    when (extractType(comment)) {
        "Link" -> Link(comment)
        "Property" -> Property(comment)
        else -> null
    }

private fun extractIncludedSchemaPath(it: Node, path: Path): Path {
    val relativePath = it.attributesMap["schemaLocation"] ?: throw IllegalStateException("Error in schema")
    val parentDirectory = path.parent.toString()
    return Paths.get(parentDirectory, relativePath)
}

fun buildSchema(path: Path): Schema {
    val schemaChildren = loadXSDSchemaFrom(path).children

    val complexTypes = schemaChildren.filter { it.nodeName == "xsd:complexType" }.toSet()
    val simpleTypes = schemaChildren.filter { it.nodeName == "xsd:simpleType" }.toSet()
    val elements = schemaChildren.filter { it.nodeName == "xsd:element" }.toSet()
    val comments = schemaChildren.filter { it.nodeName == "#comment" }
        .map { buildComment(it.nodeValue) }
        .flatten()
        .toSet()

    val includes = schemaChildren.filter { it.nodeName == "xsd:include" }
    val includedSchemas = includes.map {
        val schemaPath = extractIncludedSchemaPath(it, path)
        buildSchema(schemaPath)
    }

    var schema = Schema(complexTypes, simpleTypes, elements, comments)
    includedSchemas.forEach { schema += it }
    return schema
}

data class Schema(
    val complexTypes: Set<Node>,
    val simpleTypes: Set<Node>,
    val elements: Set<Node>,
    val comments: Set<IdlComment>
) {
    operator fun plus(other: Schema): Schema {
        return Schema(
            complexTypes + other.complexTypes,
            simpleTypes + other.simpleTypes,
            elements + other.elements,
            comments + other.comments
        )
    }
}