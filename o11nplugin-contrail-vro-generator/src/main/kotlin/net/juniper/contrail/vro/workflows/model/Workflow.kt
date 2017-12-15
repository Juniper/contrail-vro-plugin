/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

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
    displayName: String,

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

        fun includeEnd() {
            val workflowItem = WorkflowItem(name = "item0", type = "end")
            workflowItem.endMode = "0"
            workflowItem.position = Position(300.0f, 45.0f)
            workflow.workflowItems.add(workflowItem)
        }

        fun script(setup: WorkflowItem.() -> Unit) {
            val id = workflow.workflowItems.size
            val previousId = id - 1
            val scriptableItem = WorkflowItem("item$id", "task")
            scriptableItem.outName = "item$previousId"
            scriptableItem.displayName = "Scriptable task"
            scriptableItem.position = Position(200.0f, 45.0f)
            scriptableItem.setup()
            workflow.workflowItems.add(scriptableItem)
        }
    }
}

val API_VERSION = "6.0.0"
val VERSION = "\${project.version}.\${build.number}"

fun workflow(displayName: String, setup: Workflow.() -> Unit): Workflow {
    val workflow = Workflow(displayName)
    workflow.id = displayName.hashCode().toString()
    workflow.rootName = "item1"
    workflow.objectName = "workflow:name=generic"
    workflow.version = VERSION
    workflow.apiVersion = API_VERSION
    workflow.restartMode = "1"
    workflow.resumeFromFailedMode = "0"
    workflow.position = Position(100.0f, 100.0f)

    workflow.setup()

    return workflow
}
