/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.generator.ProjectInfo
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow",
    propOrder = ["displayName", "position", "input", "output", "workflowItems", "presentation"]
)
@XmlRootElement(name = "workflow")
class Workflow(
    displayName: String = "Workflow",

    @XmlElement(required = true)
    var position: Position? = null,

    @XmlElement(required = true)
    val input: ParameterSet = ParameterSet(),

    @XmlElement(required = true)
    val output: ParameterSet = ParameterSet(),

    @XmlAttribute(name = "root-name")
    var rootName: String? = null,

    @XmlAttribute(name = "object-name")
    var objectName: String? = null,

    @XmlAttribute(name = "id")
    var id: String? = null,

    @XmlAttribute(name = "version")
    var version: String? = null,

    @XmlAttribute(name = "api-version")
    var apiVersion: String? = null,

    @XmlAttribute(name = "allowed-operations")
    var allowedOperations: String? = null,

    @XmlAttribute(name = "restartMode")
    var restartMode: String? = null,

    @XmlAttribute(name = "resumeFromFailedMode")
    var resumeFromFailedMode: String? = null
) {
    @XmlElement(name = "display-name", required = true)
    val displayName: String = displayName

    @XmlElement(name = "workflow-item")
    val workflowItems: MutableList<WorkflowItem> = mutableListOf()

    @XmlElement(required = true)
    val presentation = Presentation()

    fun input(setup: ParametersBuilder.() -> Unit) {
        ParametersBuilder(input).setup()
    }

    fun output(setup: ParametersBuilder.() -> Unit) {
        ParametersBuilder(output).setup()
    }

    fun items(setup: ItemBuilder.() -> Unit) {
        ItemBuilder(this).setup()
    }

    fun presentation(setup: Presentation.() -> Unit) {
        presentation.setup()
    }

    class ParametersBuilder(private val parameters: ParameterSet) {

        fun parameter(name: String, type: String, description: String? = null) {
            parameters.addParameter(Parameter(name, type, description))
        }
    }

    class ItemBuilder(private val workflow: Workflow) {

        var includeEnd: Boolean
            get() = workflow.workflowItems.contains(END)
            set(value) {
                if (value) workflow.workflowItems.add(END) else workflow.workflowItems.remove(END)
            }

        init {
            includeEnd = true
        }

        fun script(setup: WorkflowItem.() -> Unit) {
            val id = workflow.workflowItems.size
            val previousId = id - 1
            val scriptableItem = WorkflowItem("item$id", "task")
            scriptableItem.outName = "item$previousId"
            scriptableItem.displayName = "Scriptable task"
            scriptableItem.position = Position(145.0f, 20.0f)
            scriptableItem.setup()
            workflow.workflowItems.add(scriptableItem)
        }
    }
}

private val END = WorkflowItem("item0", type = "end").apply {
    endMode = "0"
    position = Position(325.0f, 10.0f)
}

fun workflow(info: ProjectInfo, displayName: String, setup: Workflow.() -> Unit): Workflow {
    val workflow = Workflow(displayName)
    workflow.id = (info.workflowsPackageName + displayName).hashCode().toString()
    workflow.rootName = "item1"
    workflow.objectName = "workflow:name=generic"
    workflow.version = "${info.baseVersion}.${info.buildNumber}"
    workflow.apiVersion = "6.0.0"
    workflow.restartMode = "1"
    workflow.resumeFromFailedMode = "0"
    workflow.position = Position(100.0f, 100.0f)

    workflow.setup()

    return workflow
}
