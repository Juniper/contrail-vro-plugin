/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.asBackRef
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.parentConnection
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.childDescriptionInRemoveRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInRemoveRelation

internal fun addFloatingIpToPort(schema: Schema): WorkflowDefinition {
    val workflowName = addRelationWorkflowName<VirtualMachineInterface, FloatingIp>()

    return customWorkflow<VirtualMachineInterface>(workflowName).withScriptFile("addFloatingIpToPort") {
        parameter(item, reference<VirtualMachineInterface>()) {
            description = schema.parentDescriptionInCreateRelation<VirtualMachineInterface, FloatingIp>()
            mandatory = true
            browserRoot = child.parentConnection
        }
        parameter(child, reference<FloatingIp>()) {
            description = schema.childDescriptionInCreateRelation<VirtualMachineInterface, FloatingIp>(ignoreMissing = true)
            mandatory = true
            browserRoot = item.parentConnection
        }
        parameter("fixedIpAddress", boolean) {
            description = propertyDescription<FloatingIp>(schema)
            defaultValue = true
            mandatory = true
        }
    }
}

internal fun removeFloatingIpFromPort(): WorkflowDefinition {
    val workflowName = removeRelationWorkflowName<VirtualMachineInterface, FloatingIp>()

    return customWorkflow<VirtualMachineInterface>(workflowName).withScriptFile("removeFloatingIpFromPort") {
            parameter(item, reference<VirtualMachineInterface>()) {
                description = parentDescriptionInRemoveRelation<VirtualMachineInterface, FloatingIp>()
                mandatory = true
            }
            parameter(child, reference<FloatingIp>()) {
                description = childDescriptionInRemoveRelation<VirtualMachineInterface, FloatingIp>()
                mandatory = true
                visibility = WhenNonNull(item)
                browserRoot = item.asBrowserRoot()
                listedBy = actionCallTo(propertyValue).parameter(item).string(asBackRef<FloatingIp>())
            }
        }
}