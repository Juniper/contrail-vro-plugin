/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import net.juniper.contrail.vro.generator.workflows.model.Binding
import net.juniper.contrail.vro.generator.workflows.model.ParameterSet
import net.juniper.contrail.vro.generator.workflows.model.Presentation
import net.juniper.contrail.vro.generator.workflows.model.PresentationStep
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.model.WorkflowScript

infix fun WorkflowVersionInfo.withScript(body: String) =
    SimpleWorkflowBuilder(this, body)

class SimpleWorkflowBuilder(val info: WorkflowVersionInfo, val scriptBody: String)

infix fun SimpleWorkflowBuilder.andParameters(setup: PresentationParametersBuilder.() -> Unit): Workflow {

    val steps = mutableListOf<PresentationStep>()
    val parameters = mutableListOf<ParameterInfo>()
    val allParameters = mutableListOf<ParameterInfo>()

    PresentationParametersBuilder(steps, parameters, allParameters).apply(setup)

    val presentation = Presentation(steps, parameters.asPresentationParameters)

    val outBinding = Binding(listOf(success.asBind))
    val inBinding = Binding(allParameters.asBinds)

    val script = WorkflowScript(scriptBody)
    val scriptItem = scriptWorkflowItem(script, inBinding, outBinding)
    val workflowItems = listOf(END, scriptItem)
    val output = listOf(success.asParameter)

    return Workflow(
        displayName = info.nameInfo.workflowName,
        id = info.nameInfo.id,
        version = info.version,
        presentation = presentation,
        workflowItems = workflowItems,
        references = allParameters.asReferences,
        input = allParameters.asParameterSet,
        output = ParameterSet(output)
    )
}
