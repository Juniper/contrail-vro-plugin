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
    name: String,
    type: WorkflowItemType,
    position: Position,
    endMode: String? = null,
    outName: String? = null,
    displayName: String? = null
) {
    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlElement(required = true)
    val position: Position = position

    @XmlAttribute(name = "end-mode")
    var endMode: String? = endMode

    @XmlAttribute(name = "out-name")
    var outName: String? = outName

    @XmlElement(name = "script")
    private var script: WorkflowScript? = null

    @XmlElement(name = "display-name")
    var displayName: String? = displayName.CDATA
        set(value) {
            field = value.CDATA
        }

    @XmlElement(name = "in-binding")
    var inBinding: Binding? = null

    @XmlElement(name = "out-binding")
    var outBinding: Binding? = null

    var body: String?
        get() = script?.value
        set(value) {
            script = WorkflowScript(value = value, encoded = false)
        }

    fun inBinding(name: String, type: ParameterType<Any>, exportName: String = name) {
        inBinding = inBinding.safeBind(name, type, exportName)
    }

    fun outBinding(name: String, type: ParameterType<Any>, exportName: String = name) {
        outBinding = outBinding.safeBind(name, type, exportName)
    }

    private fun Binding?.safeBind(name: String, type: ParameterType<Any>, exportName: String): Binding =
        (this ?: Binding()).apply { bind(name, type, exportName) }
}

enum class WorkflowItemType {
    task,
    end,
    ;
}

val END = WorkflowItem("item0", WorkflowItemType.end, Position(330.0f, 10.0f)).apply {
    endMode = "0"
}

fun scriptWorkflowItem(id: Int, scriptBody: String) =
    WorkflowItem("item$id", WorkflowItemType.task, Position(150.0f, 20.0f)) .apply {
        body = scriptBody
        displayName = "Scriptable task"
        outName = "item${id-1}"
    }
