/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

import org.w3c.dom.Node
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

val XSD_TYPES = arrayOf("xsd:complexType", "xsd:simpleType", "xsd:element")

data class SchemaChildren(
    val complexTypes: List<Node>,
    val simpleTypes: List<Node>,
    val elements: List<Node>,
    val comments: List<Node>
)

class Schema(path: Path) {
    private val rootNode = loadXSDSchemaFrom(path)
    val schemaChildren = extractSchemaChildren()

    private fun extractSchemaChildren(): SchemaChildren {
        val children = rootNode.children
        return SchemaChildren(
            children.filter { it.nodeName == "xsd:complexType" },
            children.filter { it.nodeName == "xsd:simpleType" },
            children.filter { it.nodeName == "xsd:element" },
            children.filter { it.nodeName == "#comment" }
        )
    }

    private fun loadXSDSchemaFrom(path: Path): Node {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile())
        return document.lastChild
    }
}