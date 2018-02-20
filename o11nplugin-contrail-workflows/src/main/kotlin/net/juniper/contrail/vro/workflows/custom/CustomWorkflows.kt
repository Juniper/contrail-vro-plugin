/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.schema.Schema

// Rule edition workflows are hidden until they are needed
fun loadCustomWorkflows(schema: Schema): List<WorkflowDefinition> = mutableListOf<WorkflowDefinition>().apply {
    this += createConnectionWorkflow()
    this += deleteConnectionWorkflow()
    this += addRuleToPolicyWorkflow(schema)
//    this += editPolicyRuleWorkflow(schema)
    this += removePolicyRuleWorkflow(schema)
    this += createSubnetWorkflow(schema)
    this += deleteSubnetWorkflow()
    this += deleteFloatingIpPoolWorkflow()
    this += addRuleToSecurityGroupWorkflow(schema)
//    this += editSecurityGroupRuleWorkflow(schema)
    this += removeSecurityGroupRuleWorkflow(schema)
}