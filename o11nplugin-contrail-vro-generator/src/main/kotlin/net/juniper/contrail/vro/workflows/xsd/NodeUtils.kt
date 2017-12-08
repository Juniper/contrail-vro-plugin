/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

import org.w3c.dom.Node

val Node.children: List<Node> get() {
    val childNodeList = childNodes
    val list = ArrayList<Node>()
    var child: Node
    for (temp in 0 until childNodeList.length) {
        child = childNodeList.item(temp)
        if ("#text" != child.nodeName) {
            list.add(child)
        }
    }
    return list
}

val Node.attributesMap: HashMap<String, String>
    get() {
        if (attributes == null) return HashMap()

        val map = HashMap<String, String>()
        var attribute: Node
        for (temp in 0 until attributes.length) {
            attribute = attributes.item(temp)
            map.put(attribute.nodeName, attribute.nodeValue)
        }

        return map
    }

val Node.idlComment: Node?
    get() = findIdlComment(this)

fun findIdlComment(node: Node): Node? {
    var currentNode = node.nextSibling
    while (!XSD_TYPES.contains(currentNode.nodeName)) {
        if (currentNode.nodeName == "#comment" && currentNode.nodeValue.contains("#IFMAP-SEMANTICS-IDL")) {
            return currentNode
        }
        currentNode = currentNode.nextSibling
    }
    return null
}

fun HashSet<Node>.withAttribute(attribute: String, name: String): List<Node> {
    return this.filter { it.attributesMap[attribute]?.equals(name) ?: false }
}