/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.Binding
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

fun scriptWorkflowItem(script: Script, inBinding: Binding, outBinding: Binding, id: Int = 1) =
    WorkflowItem(id, WorkflowItemType.task, defaultScriptPosition, "Scriptable task", script, inBinding, outBinding)
