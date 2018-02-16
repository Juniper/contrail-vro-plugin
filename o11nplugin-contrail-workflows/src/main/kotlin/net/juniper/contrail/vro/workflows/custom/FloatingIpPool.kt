/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.relationDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

internal fun createFloatingIpPoolWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Create floating IP pool"

    return customWorkflow<FloatingIpPool>(workflowName).withScriptFile("createFloatingIpPool") {
        description = schema.relationDescription<VirtualNetwork, FloatingIpPool>()
        parameter("name", string) {
            description = "Floating IP Pool name"
            mandatory = true
        }
        parameter("parent", reference<VirtualNetwork>()) {
            description = "Virtual Network this floating IP pool should be related to"
            mandatory = true
        }
        parameter("projects", reference<Project>().array) {
            extractRelationDescription<Project, FloatingIpPool>(schema)
            mandatory = false
        }
        output("item", reference<FloatingIpPool>()) {
            description = "Floating IP pool created in this workflow"
        }

    }
}

internal fun deleteFloatingIpPoolWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Delete floating IP pool"

    return customWorkflow<FloatingIpPool>(workflowName).withScriptFile("deleteFloatingIpPool") {
        parameter("item", reference<FloatingIpPool>()) {
            description = "Floating IP pool to be deleted"
            mandatory = true
        }
    }
}
