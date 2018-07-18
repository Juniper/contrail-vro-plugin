/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow-itemType",
    propOrder = ["displayName", "script", "inBinding", "outBinding", "conditions", "presentation", "position"]
)

data class WorkflowItemDefinition(
    val id: Int,
    val type: WorkflowItemType,
    val position: Position,
    val displayName: String? = null,
    val script: Script? = null,
    val inBinding: Binding? = null,
    val outBinding: Binding? = null,
    val outItemId: Int? = null,
    val conditions: List<ConditionDefinition>? = null,
    val presentation: Presentation? = null,
    val linkedWorkflowId: String? = null
) {
    fun asWorkflowItem(): WorkflowItem {
        val conditions = conditions?.toConditions
        val tempScript: Script? = script ?: if (type == WorkflowItemType.switch && conditions != null) generateSwitchScript(conditions) else null
        return WorkflowItem (
            id,
            type,
            position,
            displayName,
            tempScript,
            inBinding,
            outBinding,
            outItemId,
            conditions,
            presentation,
            linkedWorkflowId
        )
    }
}

fun List<ConditionDefinition>.replaceEndLabels(newItemId: Int) =
    map {
        it.withNewTargetId(
            if (it.label == workflowEndItemId.toFullItemId) newItemId.toFullItemId else it.label
        )
    }

val List<WorkflowItemDefinition>.asWorkflowItems get() =
    map { it.asWorkflowItem() }

class WorkflowItem(
    id: Int,
    type: WorkflowItemType,
    position: Position,
    displayName: String? = null,
    script: Script? = null,
    inBinding: Binding? = null,
    outBinding: Binding? = null,
    outItemId: Int? = null,
    conditions: List<Condition>? = null,
    presentation: Presentation? = null,
    linkedWorkflowId: String? = null
) {
    @XmlAttribute(name = "name")
    val name: String = id.toFullItemId

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlElement(required = true)
    val position: Position = position

    @XmlAttribute(name = "end-mode")
    val endMode: String? = type.endMode

    @XmlAttribute(name = "out-name")
    val outName: String? = outItemId?.toFullItemId

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

    @XmlElement(name = "presentation")
    val presentation: Presentation? = presentation

    @XmlAttribute(name = "linked-workflow-id")
    val linkedWorkflowId: String? = linkedWorkflowId
}

enum class WorkflowItemType(val endMode: String?) {
    task(null),
    input(null),
    link(null), // link to a workflow that will be invoked
    switch(null),
    end("0");
}

val Int.toFullItemId get() =
    "$item$this"