/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.ConditionDefinition
import net.juniper.contrail.vro.workflows.model.Position
import net.juniper.contrail.vro.workflows.model.Presentation
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.WorkflowItemDefinition
import net.juniper.contrail.vro.workflows.model.WorkflowItemType
import net.juniper.contrail.vro.workflows.model.boolean

@DslMarker
annotation class WorkflowBuilder

val workflowEndItemId = 0
val END = WorkflowItemDefinition(workflowEndItemId, WorkflowItemType.end, Position(330.0f, 10.0f))

val success = ParameterInfo("success", boolean)

val defaultScriptPosition = Position(150.0f, 20.0f)
val defaultWorkflowPosition = Position(50.0f, 10.0f)

// TODO: set proper positions for generated workflow items
val somePosition = Position(100.0f, 0.0f)

fun scriptWorkflowItem(id: Int, script: Script, inBinding: Binding, outBinding: Binding, outId: Int) =
    WorkflowItemDefinition(id, WorkflowItemType.task, defaultScriptPosition, "Scriptable task", script, inBinding, outBinding, outId)

fun workflowWorkflowItem(id: Int, workflowName: String, workflowId: String, inBinding: Binding, outBinding: Binding, outId: Int) =
    WorkflowItemDefinition(id, WorkflowItemType.link, somePosition, workflowName, null, inBinding, outBinding, outId, null, null, workflowId)

fun switchWorkflowItem(id: Int, inBinding: Binding, conditions: List<ConditionDefinition>) =
    WorkflowItemDefinition(id, WorkflowItemType.switch, somePosition, "Switch", null, inBinding, null, null, conditions)

fun inputWorkflowItem(id: Int, inBinding: Binding, outBinding: Binding, outId: Int, preparedPresentation: Presentation? = null, parameterDefinitions: ParameterAggregator.() -> Unit = {}): WorkflowItemDefinition {
    val presentation = if (preparedPresentation == null) {
        val parameters = mutableListOf<ParameterInfo>()
        val allParameters = mutableListOf<ParameterInfo>()
        ParameterAggregator(parameters, allParameters).apply(parameterDefinitions)
        Presentation(parameters.map { it.asPresentationParameter })
    } else {
        preparedPresentation
    }
    return WorkflowItemDefinition(id, WorkflowItemType.input, somePosition, "User interaction", null, inBinding, outBinding, outId, null, presentation)
}