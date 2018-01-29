/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import net.juniper.contrail.vro.generator.workflows.util.generateID
import net.juniper.contrail.vro.generator.workflows.model.Binding
import net.juniper.contrail.vro.generator.workflows.model.ParameterSet
import net.juniper.contrail.vro.generator.workflows.model.Position
import net.juniper.contrail.vro.generator.workflows.model.Presentation
import net.juniper.contrail.vro.generator.workflows.model.PresentationStep
import net.juniper.contrail.vro.generator.workflows.model.Reference
import net.juniper.contrail.vro.generator.workflows.model.Script
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.model.WorkflowItem

data class WorkflowDefinition(
    val displayName: String,
    val presentation: Presentation = Presentation(),
    val workflowItems: List<WorkflowItem> = emptyList(),
    val references: List<Reference>? = null,
    val input: ParameterSet = ParameterSet(),
    val output: ParameterSet = ParameterSet(),
    val position: Position = defaultWorkflowPosition
) {
    fun createWorkflow(packageName: String, version: String): Workflow {
        if (workflowItems.isEmpty())
            throw IllegalStateException("No workflow items were defined.")
        return toWorkflow(packageName, version)
    }

    private fun toWorkflow(packageName: String, version: String) = Workflow (
        displayName = displayName,
        id = generateID(packageName, displayName),
        version = version,
        presentation = presentation,
        workflowItems = workflowItems,
        references = references,
        input = input,
        output = output,
        position = position
    )
}

fun workflow(name: String) =
    WorkflowDefinition(name)

fun WorkflowDefinition.withScript(scriptBody: String, setup: PresentationParametersBuilder.() -> Unit): WorkflowDefinition {
    if (!workflowItems.isEmpty())
        throw IllegalStateException("Script was already defined for this workflow.")

    val steps = mutableListOf<PresentationStep>()
    val parameters = mutableListOf<ParameterInfo>()
    val allParameters = mutableListOf<ParameterInfo>()

    val builder = PresentationParametersBuilder(steps, parameters, allParameters).apply(setup)

    val presentation = Presentation(parameters.asPresentationParameters, steps, builder.description)

    val outBinding = Binding(listOf(success.asBind))
    val inBinding = Binding(allParameters.asBinds)

    val script = Script(scriptBody)
    val scriptItem = scriptWorkflowItem(script, inBinding, outBinding)
    val workflowItems = listOf(END, scriptItem)
    val output = listOf(success.asParameter)

    return copy(
        presentation = presentation,
        workflowItems = workflowItems,
        references = allParameters.asReferences,
        input = allParameters.asParameterSet,
        output = ParameterSet(output)
    )
}

