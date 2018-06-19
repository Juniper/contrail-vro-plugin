/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import net.juniper.contrail.vro.config.constants.item
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow-itemType",
    propOrder = ["displayName", "script", "inBinding", "outBinding", "condition", "position"]
)
class WorkflowItem(
    id: Int,
    type: WorkflowItemType,
    position: Position,
    displayName: String? = null,
    script: Script? = null,
    inBinding: Binding? = null,
    outBinding: Binding? = null,
    outItemId: Int? = null,
    conditions: List<Condition>? = null
) {
    @XmlAttribute(name = "name")
    val name: String = "$item$id"

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlElement(required = true)
    val position: Position = position

    @XmlAttribute(name = "end-mode")
    val endMode: String? = type.endMode

    @XmlAttribute(name = "out-name")
    val outName: String? = outItemId?.let { "$item$it" }

    @XmlElement(name = "script")
    val script: Script? = script

    @XmlElement(name = "display-name")
    val displayName: String? = displayName.CDATA

    @XmlElement(name = "in-binding")
    val inBinding: Binding? = inBinding

    @XmlElement(name = "out-binding")
    val outBinding: Binding? = outBinding

    @XmlElement(name = "condition")
    val conditions: List<Condition>? = conditions
}

enum class WorkflowItemType(val endMode: String?) {
    task(null),
    input(null),
    link(null), // link to a workflow that will be invoked
    switch(null),
    end("0");
}
