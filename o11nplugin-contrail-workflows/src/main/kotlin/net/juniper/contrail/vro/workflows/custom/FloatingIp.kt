/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.createSimpleWorkflowName
import net.juniper.contrail.vro.workflows.util.relationDescription

internal fun createFloatingIpWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<FloatingIp>(createSimpleWorkflowName<FloatingIp>()).withScriptFile("createFloatingIp") {
        description = relationDescription<FloatingIpPool, FloatingIp>(schema)
        parameter(parent, reference<FloatingIpPool>()) {
            description = "Floating IP pools this IP will belong to"
            mandatory = true
        }
        parameter("projects", reference<Project>().array) {
            description = relationDescription<FloatingIp, Project>(schema)
        }
        parameter("address", string) {
            description = "IP address\n Will be allocated dynamically if this input is left empty."
            mandatory = false
            validWhen = isIPAddress()
        }
        output(item, reference<FloatingIp>()) {
            description = "Floating IP created in this workflow"
        }
    }

