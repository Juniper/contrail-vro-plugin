/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.inputWorkflowItem
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.string

val start = 1
val choice1 = 2
val choice2 = 3
val item = 4

fun someComplexWorkflow() : WorkflowDefinition {
    val workflowDefinitions = mutableListOf<WorkflowDefinition>()
    return workflow("Some complex workflow").withComplexParameters(start, workflowDefinitions) {

        choice(start, choice1, "Do you want to go back to start?") {
            option("Yes!", start)
            option("No", choice1)
        }

        choice(choice1, choice2, "Are you sure?") {
            option("Yes", choice2)
            option("No", start)
        }

        choice(choice2, END.id, "Exit?") {
            option("Yes", END.id)
            option("No", start)
        }
    }
}

fun someSimpleWorkflow() : WorkflowDefinition = workflow("Some simple workflow").withScript("") { }

fun someComplexWorkflowWithAutomaticOutput(choices: Int = 0, inputItems: Int = 1, attributeNames: List<String> = listOf(), parameterNames: List<String> = listOf()): WorkflowDefinition {
    val workflowDefinitions = mutableListOf<WorkflowDefinition>()
    return workflow("Some complex workflow").withComplexParameters(start, workflowDefinitions) {

        attributeNames.forEach { attribute(it, string) }

        if (choices > 0) {
            (1..choices).forEach {
                choice(it, workflowEndItemId, "") {
                    option("", workflowEndItemId)
                }
            }
        }

        if (inputItems > 0) {
            (1..inputItems).forEach { items.add(inputWorkflowItem(choices + it, Binding(listOf()), Binding(listOf()), workflowEndItemId)) }
        }

        val attributeAndParameterPairs = attributeNames.zip(parameterNames)

        automaticWorkflowOutput {
            attributeAndParameterPairs.forEach { output(it.first, it.second) }
        }
    }
}
