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
import net.juniper.contrail.vro.workflows.model.WorkflowItemDefinition
import net.juniper.contrail.vro.workflows.model.WorkflowItemType
import net.juniper.contrail.vro.workflows.model.asWorkflowItem
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
    val position: Position = defaultPosition,
    val rootId: Int = 0
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
        position = position,
        rootItemId = rootId
    )
}

fun workflow(name: String) =
    WorkflowDefinition(name)

fun WorkflowDefinition.inCategory(category: String) =
    copy(category = category)

fun fixItemsPositions(items: List<WorkflowItemDefinition>, horizontalTranslation: Float, verticalTranslation: Float): List<WorkflowItemDefinition> {
    return items.mapIndexed { idx, it ->
        it.copy(position = generateTruePosition(idx, horizontalTranslation, verticalTranslation, it.type))
    }
}

fun generateTruePosition(idx: Int, horizontalTranslation: Float, verticalTranslation: Float, type: WorkflowItemType): Position {
    val neededTranslations = idx + 1

    val newX = calculateHorizontalPosition(neededTranslations, horizontalTranslation) + horizontalOffset(type)
    val newY = calculateVerticalPosition(idx, verticalTranslation) + verticalOffset(type)

    return Position(newX, newY)
}

fun calculateHorizontalPosition(numberOfTranslations: Int, sizeOfTranslation: Float): Float = defaultX + (numberOfTranslations * sizeOfTranslation)

fun calculateVerticalPosition (idx: Int, sizeOfTranslation: Float): Float {
    val verticalPosition =
        if (idx % 2 == 0)
            defaultY + sizeOfTranslation
        else
            defaultY + (sizeOfTranslation * -1)
    return verticalPosition
}

fun horizontalOffset(type: WorkflowItemType): Float {
    val endHorizontalTranslation = 80.0f
    val offset =
        if (type == WorkflowItemType.end)
            endHorizontalTranslation
        else
            0f
    return offset
}

fun verticalOffset(type: WorkflowItemType): Float {
    val taskVerticalTranslation = 10.0f
    val offset =
        if (type == WorkflowItemType.task)
            taskVerticalTranslation
        else
            0f
    return offset
}

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
    val scriptItemId = 1
    val scriptItem = scriptWorkflowItem(scriptItemId, script, inBinding, outBinding, workflowEndItemId)
    val workflowItemsDefinitions = listOf(scriptItem, END)

    // vertical translation is 0 because we have only 3 items
    val horizontalTranslation = 100.0f
    val verticalTranslation = 0f

    val fixedPositionItems = fixItemsPositions(workflowItemsDefinitions, horizontalTranslation, verticalTranslation)
    val workflowItems = fixedPositionItems.map { it.asWorkflowItem() }

    return copy(
        presentation = presentation,
        workflowItems = workflowItems,
        references = allParameters.asReferences,
        input = allParameters.asParameterSet,
        output = outputParameters.asParameterSet,
        attributes = attributes,
        rootId = scriptItemId
    )
}

fun WorkflowDefinition.withComplexParameters(rootItemId: Int, workflowDefinitions: List<WorkflowDefinition>, setup: ComplexWorkflowBuilder.() -> Unit): WorkflowDefinition {
    if (!workflowItems.isEmpty())
        throw IllegalStateException("This workflow is already set up")

    val builder = ComplexWorkflowBuilder(workflowDefinitions).apply(setup)
    val items = builder.items
    val attributes = builder.attributes

    items.add(END)

    // TODO: first user interaction can be replaced with workflow input
    val initialPresentation = Presentation()
    val initialInput = listOf<ParameterInfo>()

    val horizontalTranslation = 75.0f
    val verticalTranslation = 70.0f
    val fixedPositionItems = fixItemsPositions(items, horizontalTranslation, verticalTranslation)
    val workflowItems = fixedPositionItems.map { it.asWorkflowItem() }

    return copy(
        presentation = initialPresentation,
        workflowItems = workflowItems,
        references = listOf(),
        input = initialInput.asParameterSet,
        output = listOf<ParameterInfo>().asParameterSet,
        attributes = attributes,
        rootId = rootItemId
    ).inCategory("Complex Examples")
}