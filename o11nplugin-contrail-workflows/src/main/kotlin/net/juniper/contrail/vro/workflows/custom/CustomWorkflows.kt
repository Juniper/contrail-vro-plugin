/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.schema.Schema

fun loadCustomWorkflows(schema: Schema): List<WorkflowDefinition> = mutableListOf<WorkflowDefinition>().apply {
    this += createConnectionWorkflow()
    this += deleteConnectionWorkflow()
    this += addPolicyToVirtualNetwork(schema)
    this += addRuleToPolicyWorkflow(schema)
    this += removePolicyRuleWorkflow(schema)
    this += createSubnetWorkflow(schema)
    this += deleteSubnetWorkflow()
    this += createFloatingIpWorkflow(schema)
    this += addPortToFloatingIpWorkflow(schema)
    this += addRuleToSecurityGroupWorkflow(schema)
    this += removeSecurityGroupRuleWorkflow(schema)
    this += addPortTupleToServiceInstance(schema)
    this += createServiceTemplate(schema)
    this += removePortTupleFromServiceInstance()
    this += createServiceInstance(schema)
    this += addAllowedAddressPair(schema)
    this += removeAllowedAddressPair(schema)
}