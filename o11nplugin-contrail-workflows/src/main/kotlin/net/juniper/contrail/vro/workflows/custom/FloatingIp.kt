/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.workflows.schema.relationDescription
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

internal fun createFloatingIpWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Create floating IP"

    return customWorkflow<FloatingIp>(workflowName).withScriptFile("createFloatingIp") {
        description = schema.relationDescription<FloatingIpPool, FloatingIp>()
        parameter(parent, reference<FloatingIpPool>()) {
            description = "Floating IP pools this IP will belong to"
            mandatory = true
        }
        parameter("projects", reference<Project>().array) {
            extractRelationDescription<FloatingIp, Project>(schema)
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
}

internal fun addPortToFloatingIpWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add port to floating IP"

    return customWorkflow<FloatingIp>(workflowName).withScriptFile("addPortToFloatingIp") {
        description = "Add port to floating IP"
        parameter(item, reference<FloatingIp>()) {
            description = "Floating IP to add port to"
            mandatory = true
        }
        parameter("port", reference<VirtualMachineInterface>()) {
            extractRelationDescription<FloatingIp, VirtualMachineInterface>(schema)
            mandatory = true
        }
        parameter("fixedIpAddress", boolean) {
            extractPropertyDescription<FloatingIp>(schema)
            defaultValue = true
            mandatory = true
        }
    }
}