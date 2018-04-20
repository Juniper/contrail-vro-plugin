/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

import org.w3c.dom.Node

val Node.children: Sequence<Node> get() = childNodes.run {
    (0 until length).asSequence()
        .map { item(it) }
        .filter { it.nodeName != textName }
}

val Node.nestedChildren: Sequence<Node> get() = children
    .map { it.nestedChildren + it }
    .flatten()

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

val Node.nameAttribute: String? get() =
    attributeValue(name)

val Node.typeAttribute: String? get() =
    attributeValue(type)

val Node.descriptionAttribute: String? get() =
    attributeNodes
        .filter { it.nodeName.startsWith(description) }
        .map { it.nodeValue }.filterNotNull()
        .joinToString("\n")
        .let { if (it.isBlank()) null else it }

val Node.baseAttribute: String get() =
    attributeValue(base) ?: throw IllegalStateException(
        "Mandatory attribute '$base' was not found in restriction of ${parentNode.parentNode.nameAttribute}."
    )

val Node.restrictionNode: Node get() =
    nestedChildren.find { it.nodeName == xsdRestriction } ?: throw IllegalStateException(
        "Restriction node was not found in $nameAttribute."
    )

val Node.restrictionType: String? get() =
    restrictionNode.baseAttribute

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

fun Iterable<Node>.theOneNamed(theName: String): Node =
    asSequence().theOneNamed(theName)

fun Iterable<Node>.theOneNamedOrNull(theName: String): Node? =
    asSequence().theOneNamedOrNull(theName)

fun Sequence<Node>.withName(theName: String) =
    withAttribute(name, theName.stripSmi)

fun Sequence<Node>.theOneNamed(theName: String): Node =
    withName(theName).theOne(theName)

fun Sequence<Node>.theOneNamedOrNull(theName: String): Node? =
    withName(theName).firstOrNull()

fun Sequence<Node>.theOne(theName: String): Node =
    firstOrNull() ?:
        throw IllegalArgumentException("Definition of $theName was not found in the schema.")

inline fun Iterable<Node>.withAttribute(attribute: String, crossinline condition: (String) -> Boolean) =
    asSequence().withAttribute(attribute, condition).toList()

fun Sequence<Node>.withAttribute(attribute: String, value: String): Sequence<Node> =
    withAttribute(attribute) { it.equals(value, ignoreCase = true) }

inline fun Sequence<Node>.withAttribute(attribute: String, crossinline condition: (String) -> Boolean): Sequence<Node> =
    filter { it.attributeValue(attribute)?.run(condition) ?: false }