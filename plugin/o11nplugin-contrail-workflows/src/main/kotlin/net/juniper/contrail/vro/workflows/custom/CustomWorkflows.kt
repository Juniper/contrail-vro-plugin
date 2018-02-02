/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.schema.Schema

fun loadCustomWorkflows(schema: Schema): List<WorkflowDefinition> = mutableListOf<WorkflowDefinition>().apply {
    this += createConnectionWorkflow()
    this += deleteConnectionWorkflow()
    this += addRuleToPolicyWorkflow(schema)
    this += editPolicyRuleWorkflow(schema)
    this += createSubnetWorkflow(schema)
}