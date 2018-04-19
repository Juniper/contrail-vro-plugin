/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.schema.Schema

// Rule edition workflows are hidden until they are needed
fun loadCustomWorkflows(schema: Schema): List<WorkflowDefinition> = mutableListOf<WorkflowDefinition>().apply {
    this += createConnectionWorkflow()
    this += deleteConnectionWorkflow()
    this += addPolicyToVirtualNetwork(schema)
    this += addRuleToPolicyWorkflow(schema)
    this += editPolicyRuleWorkflow(schema)
    this += removePolicyRuleWorkflow(schema)
    this += createSubnetWorkflow(schema)
    this += deleteSubnetWorkflow()
    this += createFloatingIpWorkflow(schema)
    this += addFloatingIpToPort(schema)
    this += removeFloatingIpFromPort()
    this += addRuleToSecurityGroupWorkflow(schema)
    this += editSecurityGroupRuleWorkflow(schema)
    this += removeSecurityGroupRuleWorkflow(schema)
    this += addPortTupleToServiceInstance(schema)
    this += createServiceTemplate(schema)
    this += removePortTupleFromServiceInstance()
    this += createServiceInstance(schema)
    this += removeNetworkIpamSubnetWorkflow(schema)
    this += createNetworkIpamSubnetWorkflow(schema)
    this += addFlatIpamWorkflow(schema)
    this += removeFlatIpamWorkflow()
}