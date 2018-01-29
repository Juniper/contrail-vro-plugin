/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.schema

import org.w3c.dom.Node
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory

private fun loadXSDSchemaFrom(path: Path): Node {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile())
    return document.lastChild
}

private fun String.buildComments(): List<IdlComment> =
    split(";").mapNotNull { it.toCommentObject() }

private fun String.toCommentObject(): IdlComment? =
    when (extractCommentType()) {
        idlLink -> Link(this)
        idlProperty -> Property(this)
        else -> null
    }

private fun extractIncludedSchemaPath(it: Node, path: Path): Path {
    val relativePath = it.attributesMap["schemaLocation"] ?: throw IllegalStateException("Error in schema")
    val parentDirectory = path.parent.toString()
    return Paths.get(parentDirectory, relativePath)
}

fun buildSchema(path: Path): Schema {
    val rootNode = loadXSDSchemaFrom(path)
    val schemaChildren = rootNode.children

    val complexTypes = schemaChildren.filter { it.nodeName == xsdComplexType }.toSet()
    val simpleTypes = schemaChildren.filter { it.nodeName == xsdSimpleType }.toSet()
    val elements = schemaChildren.filter { it.nodeName == xsdElement }.toSet()
    val comments = schemaChildren.filter { it.nodeName == commentName }
        .map { it.nodeValue.buildComments() }
        .flatten()
        .toSet()

    val includes = schemaChildren.filter { it.nodeName == xsdInclude }
    val includedSchemas = includes.map {
        val schemaPath = extractIncludedSchemaPath(it, path)
        buildSchema(schemaPath)
    }

    val schema = Schema(complexTypes, simpleTypes, elements, comments)
    return includedSchemas.fold(schema) { accumulated, next -> accumulated + next }
}

class Schema(
    val complexTypes: Set<Node>,
    val simpleTypes: Set<Node>,
    val elements: Set<Node>,
    val comments: Set<IdlComment>
) {
    operator fun plus(other: Schema) = Schema(
        complexTypes + other.complexTypes,
        simpleTypes + other.simpleTypes,
        elements + other.elements,
        comments + other.comments
    )
}