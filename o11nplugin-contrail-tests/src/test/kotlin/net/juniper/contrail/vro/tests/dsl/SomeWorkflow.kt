/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import net.juniper.contrail.vro.workflows.model.string

val start = 1
val choice1 = 2
val choice2 = 3
val item = 4
val workflowDefinitions = mutableListOf<WorkflowDefinition>()

fun someComplexWorkflow() : WorkflowDefinition {
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

fun someWorkflowWithInputItem(attributeNames: List<String> = listOf("attribute1")): WorkflowDefinition {
    return workflow("Some workflow with inputItem").withComplexParameters(start, workflowDefinitions) {

        //The Attribute type here is arbitrary but has to be the same for all attributes because the parameter() function doesn't support polymorphism dependent on type.
        attributeNames.forEach { attribute(it, string) }

        attributeInput(start, END.id) {
            attributeNames.forEach { parameter(it, string) {} }
        }

    }
}

fun someComplexWorkflowWithInputChoiceAutomaticOutput(choices: Int = 0, inputItems: Int = 1, attributeNames: List<String> = listOf("Attribute1"), parameterNames: List<String> = listOf("Parameter1")): WorkflowDefinition {
    return workflow("Some complex workflow").withComplexParameters(1, workflowDefinitions) {
        require(choices > 0 || inputItems > 0) { "There must be at least 1 Item with a connection to EndItem in this workflow" }

        attributeNames.forEach { attribute(it, string) }

        (1..choices).forEach {
            choice(it, workflowEndItemId, "") {
                option("", workflowEndItemId)
            }
        }

        (1..inputItems).forEach { attributeInput(choices + it, workflowEndItemId) {} }

        val attributeAndParameterPairs = attributeNames.zip(parameterNames)

        automaticWorkflowOutput {
            attributeAndParameterPairs.forEach { output(it.first, it.second) }
        }
    }
}
