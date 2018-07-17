/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.date
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.string

val workflowId = 99
val start = 1
val choice1 = 2
val choice2 = 3

fun someComplexWorkflow() : WorkflowDefinition {
    val workflowDefinitions = mutableListOf<WorkflowDefinition>()
    return workflow("Some complex workflow").withComplexParameters(workflowId, workflowDefinitions) {

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

val attributeNames = listOf("parameter1", "parameter2", "parameter3", "parameter4")

fun someWorkflowWithInputItem(): WorkflowDefinition {
    val workflowDefinitions = mutableListOf<WorkflowDefinition>()
    return workflow("Some workflow with inputItem").withComplexParameters(workflowId + 1, workflowDefinitions) {
        attribute(attributeNames[0], string)
        attribute(attributeNames[1], number)
        attribute(attributeNames[2], boolean)
        attribute(attributeNames[3], date)

        addWorkflowItemWithAttributes(start, END.id) {
            parameter(attributeNames[0], string) {}
            parameter(attributeNames[1], number) {}
            parameter(attributeNames[2], boolean) {}
            parameter(attributeNames[3], date) {}
        }

    }
}