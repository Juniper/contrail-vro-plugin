/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.Attribute
import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.ParameterSet
import net.juniper.contrail.vro.workflows.model.Position
import net.juniper.contrail.vro.workflows.model.Presentation
import net.juniper.contrail.vro.workflows.model.PresentationStep
import net.juniper.contrail.vro.workflows.model.Reference
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.Workflow
import net.juniper.contrail.vro.workflows.model.WorkflowItem
import net.juniper.contrail.vro.workflows.util.generateID

data class WorkflowDefinition(
    val displayName: String,
    val category: String? = null,
    val presentation: Presentation = Presentation(),
    val workflowItems: List<WorkflowItem> = emptyList(),
    val references: List<Reference>? = null,
    val input: ParameterSet = ParameterSet(),
    val output: ParameterSet = ParameterSet(),
    val attributes: List<Attribute> = emptyList(),
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
        attributes = attributes,
        position = position
    )
}

fun workflow(name: String) =
    WorkflowDefinition(name)

fun WorkflowDefinition.inCategory(category: String) =
    copy(category = category)

typealias ParameterDefinition = PresentationParametersBuilder.() -> Unit

fun WorkflowDefinition.withScript(scriptBody: String, setup: ParameterDefinition): WorkflowDefinition {
    if (!workflowItems.isEmpty())
        throw IllegalStateException("Script was already defined for this workflow.")

    val steps = mutableListOf<PresentationStep>()
    val parameters = mutableListOf<ParameterInfo>()
    val allParameters = mutableListOf<ParameterInfo>()
    val outputParameters = mutableListOf<ParameterInfo>()
    val attributes = mutableListOf<Attribute>()

    val builder = PresentationParametersBuilder(steps, parameters, allParameters, outputParameters, attributes).apply(setup)

    val presentation = Presentation(parameters.asPresentationParameters, steps, builder.description)

    val outBinding = Binding(outputParameters.asBinds)
    val inBinding = Binding(allParameters.asBinds)

    val script = Script(scriptBody)
    val scriptItem = scriptWorkflowItem(script, inBinding, outBinding, 1, 0)
    val workflowItems = listOf(END, scriptItem)

    return copy(
        presentation = presentation,
        workflowItems = workflowItems,
        references = allParameters.asReferences,
        input = allParameters.asParameterSet,
        output = outputParameters.asParameterSet,
        attributes = attributes
    )
}

typealias ComplexParameterDefinition = ComplexWorkflowBuilder.() -> Unit

fun ComplexWorkflowDefinition(setup: ComplexParameterDefinition): WorkflowDefinition {

}