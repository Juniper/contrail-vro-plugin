/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.Condition
import net.juniper.contrail.vro.workflows.model.DefaultCondition
import net.juniper.contrail.vro.workflows.model.EqualsCondition
import net.juniper.contrail.vro.workflows.model.Position
import net.juniper.contrail.vro.workflows.model.Presentation
import net.juniper.contrail.vro.workflows.model.WorkflowItem
import net.juniper.contrail.vro.workflows.model.WorkflowItemType
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.string

@DslMarker
annotation class WorkflowBuilder

val endId = 0
val END = WorkflowItem(endId, WorkflowItemType.end, Position(330.0f, 10.0f))

val success = ParameterInfo("success", boolean)

val defaultScriptPosition = Position(150.0f, 20.0f)
val defaultWorkflowPosition = Position(50.0f, 10.0f)
val somePosition = Position(100.0f, 0.0f)

fun scriptWorkflowItem(script: Script, inBinding: Binding, outBinding: Binding, id: Int, outId: Int) =
    WorkflowItem(id, WorkflowItemType.task, defaultScriptPosition, "Scriptable task", script, inBinding, outBinding, outId)

fun workflowWorkflowItem(id: Int, workflowName: String, workflowId: String, inBinding: Binding, outBinding: Binding, outId: Int) =
    WorkflowItem(id, WorkflowItemType.link, somePosition, workflowName, null, inBinding, outBinding, outId, null, null, workflowId)

// TODO: use some 'choice' class instead of 'condition' and generate conditions and script based on it
fun switchWorkflowItem(id: Int, inBinding: Binding, conditions: List<Condition>) =
    WorkflowItem(id, WorkflowItemType.switch, somePosition, "Switch", generateSwitchScript(conditions), inBinding, null, null, conditions)

fun inputWorkflowItem(id: Int, inBinding: Binding, outBinding: Binding, outId: Int, preparedPresentation: Presentation? = null, parameterDefinitions: ParameterAggregator.() -> Unit = {}): WorkflowItem {
    val parameters = mutableListOf<ParameterInfo>()
    val allParameters = mutableListOf<ParameterInfo>()
    val presentation = if (preparedPresentation == null) {
        ParameterAggregator(parameters, allParameters).apply(parameterDefinitions)
        Presentation(parameters.map { it.asPresentationParameter })
    } else {
        preparedPresentation
    }
    return WorkflowItem(id, WorkflowItemType.input, somePosition, "User interaction", null, inBinding, outBinding, outId, null, presentation)
}

private fun generateSwitchScript(conditions: List<Condition>): Script =
    Script(conditions.joinToString(" else ") { it.toJS() })

private fun Condition.toJS(): String {
    val conditionValue: String = when (type) {
        string.name -> "\"$value\""
        else -> value
    }
    val conditionLabel: String = "\"$label\""
    return when (this) {
        is EqualsCondition ->
            """if ($name == $conditionValue) {
                |    return $conditionLabel;
                |}
            """.trimMargin()
        is DefaultCondition ->
            """if (true) {
                |    return $conditionLabel;
                |}
            """.trimMargin()
        else -> ""
    }
}