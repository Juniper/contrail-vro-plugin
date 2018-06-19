package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.constants.basePackageName
import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow

val choiceItemId = 5
val workflowItemId = 8

internal fun createAPS(workflowDefinitions: List<WorkflowDefinition>): WorkflowDefinition =
    workflow("Create APS").withComplexParameters(choiceItemId, workflowDefinitions) {
        choice(choiceItemId, "Choose stuff", listOf(Pair("finish", END.name), Pair("don't", "item$choiceItemId")), END.name)
        workflowInvokation(workflowItemId, basePackageName, "Add rule to network policy", choiceItemId, mapOf())
}