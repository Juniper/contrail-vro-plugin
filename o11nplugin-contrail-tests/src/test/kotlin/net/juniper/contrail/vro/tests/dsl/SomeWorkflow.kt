/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow

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
