/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.xsd

import org.w3c.dom.Node

val Node.children: Sequence<Node> get() = childNodes.run {
    (0 until length).asSequence()
        .map { item(it) }
        .filter { it.nodeName != textName }
}

val Node.nestedElements: Sequence<Node> get() = children
    .map { it.nestedElements + it }
    .flatten()
    .filter { it.nodeName == xsdElement }

val Node.attributesMap: Map<String, String> get() =
    attributeNodes.associateBy(Node::getNodeName, Node::getNodeValue)

val Node.attributeNodes: Sequence<Node> get() = attributes?.run {
    (0 until length).asSequence()
        .map { item(it) }
} ?: emptySequence()

fun Node.attribute(name: String): Node? =
    attributeNodes.find { it.nodeName == name }

val Node.description: String? get() =
    attributeValue("description")

fun Node.attributeValue(name: String): String? =
    attribute(name)?.nodeValue

val Node.idlComment: Node? get() {
    var currentNode = nextSibling
    while (!xsdTypes.contains(currentNode.nodeName)) {
        if (currentNode.nodeName == commentName && currentNode.nodeValue.contains(ifmapIdlName)) {
            return currentNode
        }
        currentNode = currentNode.nextSibling
    }
    return null
}

fun Iterable<Node>.withAttribute(attribute: String, name: String) =
    asSequence().withAttribute(attribute, name).toList()

fun Sequence<Node>.withAttribute(attribute: String, value: String): Sequence<Node> =
    filter { it.attributeValue(attribute) == value }