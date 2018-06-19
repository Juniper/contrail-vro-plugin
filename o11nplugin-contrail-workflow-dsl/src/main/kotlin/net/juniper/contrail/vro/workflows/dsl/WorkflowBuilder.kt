/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.Condition
import net.juniper.contrail.vro.workflows.model.Position
import net.juniper.contrail.vro.workflows.model.WorkflowItem
import net.juniper.contrail.vro.workflows.model.WorkflowItemType
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.boolean

@DslMarker
annotation class WorkflowBuilder

val END = WorkflowItem(0, WorkflowItemType.end, Position(330.0f, 10.0f))

val success = ParameterInfo("success", boolean)

val defaultScriptPosition = Position(150.0f, 20.0f)
val defaultWorkflowPosition = Position(50.0f, 10.0f)
val somePosition = Position(100.0f, 0.0f)

fun scriptWorkflowItem(script: Script, inBinding: Binding, outBinding: Binding, id: Int, outId: Int) =
    WorkflowItem(id, WorkflowItemType.task, defaultScriptPosition, "Scriptable task", script, inBinding, outBinding, outId)

// TODO: displayName
fun workflowWorkflowItem(workflowId: String, inBinding: Binding, outBinding: Binding, id: Int, outId: Int) =
    WorkflowItem(id, WorkflowItemType.link, somePosition, workflowId, null, inBinding, outBinding, outId)

// TODO: use some 'choice' class instead of 'condition' and generate conditions and script based on it
fun switchWorkflowItem(id: Int, inBinding: Binding, conditions: List<Condition>) =
    WorkflowItem(id, WorkflowItemType.link, somePosition, "Switch", generateSwitchScript(), inBinding, null, null, conditions)

// TODO: add presentation
fun inputWorkflowItem(id: Int, inBinding: Binding, outBinding: Binding, outId: Int) =
    WorkflowItem(id, WorkflowItemType.input, somePosition, "User interaction", null, inBinding, outBinding, outId, null)

// TODO
private fun generateSwitchScript(): Script =
    Script("test", false)