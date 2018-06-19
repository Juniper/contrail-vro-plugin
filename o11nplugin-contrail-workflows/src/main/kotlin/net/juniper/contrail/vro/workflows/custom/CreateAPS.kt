package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow

val choiceItemId = 5

// TODO: choose item ID and generate name based on it
internal fun createAPS(): WorkflowDefinition = workflow("Create APS").withComplexParameters(choiceItemId) {
    choice(choiceItemId, "Choose stuff", listOf(Pair("finish", END.name), Pair("don't", "item$choiceItemId")), END.name)
}