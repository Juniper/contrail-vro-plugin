package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.vro.config.constants.basePackageName
import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.reference

val choiceItemId = 5
val workflowItemId = 8
val anotherWorkflowItemId = 10
val policyAttributeName = "somePolicyWeCreated"

internal fun createAPS(workflowDefinitions: List<WorkflowDefinition>): WorkflowDefinition =
    workflow("Create APS").withComplexParameters(choiceItemId, workflowDefinitions) {
        attribute(policyAttributeName, reference<NetworkPolicy>())
        choice(choiceItemId, "Choose stuff", listOf(Pair("finish", END.name), Pair("don't", "item$workflowItemId")), END.name)
        workflowInvokation(workflowItemId, basePackageName, "Create network policy", anotherWorkflowItemId, mapOf(), mapOf(Pair("item", policyAttributeName)))
        workflowInvokation(anotherWorkflowItemId, basePackageName, "Add rule to network policy", choiceItemId, mapOf(Pair("item", policyAttributeName)), mapOf())
    }