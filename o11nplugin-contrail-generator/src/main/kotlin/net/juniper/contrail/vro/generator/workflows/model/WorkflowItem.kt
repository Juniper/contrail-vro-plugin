/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

import net.juniper.contrail.vro.generator.util.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow-itemType",
    propOrder = ["displayName", "script", "inBinding", "outBinding", "position"]
)
class WorkflowItem(
    id: Int,
    type: WorkflowItemType,
    position: Position,
    displayName: String? = null,
    script: WorkflowScript? = null,
    inBinding: Binding? = null,
    outBinding: Binding? = null
) {
    @XmlAttribute(name = "name")
    val name: String = "item$id"

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlElement(required = true)
    val position: Position = position

    @XmlAttribute(name = "end-mode")
    val endMode: String? = type.endMode

    @XmlAttribute(name = "out-name")
    val outName: String? = if (id == 0) null else "item${id - 1}"

    @XmlElement(name = "script")
    val script: WorkflowScript? = script

    @XmlElement(name = "display-name")
    val displayName: String? = displayName.CDATA

    @XmlElement(name = "in-binding")
    val inBinding: Binding? = inBinding

    @XmlElement(name = "out-binding")
    val outBinding: Binding? = outBinding
}

enum class WorkflowItemType(val endMode: String?) {
    task(null),
    end("0");
}
