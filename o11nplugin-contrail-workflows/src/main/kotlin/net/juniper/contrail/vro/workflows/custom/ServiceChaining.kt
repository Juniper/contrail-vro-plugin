/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.config.getPortsForServiceInterface
import net.juniper.contrail.vro.config.serviceHasInterfaceWithName
import net.juniper.contrail.vro.config.getPortTuplesOfServiceInstance
import net.juniper.contrail.vro.workflows.dsl.FromActionVisibility
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.schema.createWorkflowDescription

val maxOtherInterfacesSupported = 8
val supportedOtherInterfaces = (0 until maxOtherInterfacesSupported).map { "other$it" }
val supportedInterfaceNames = listOf(
    "left",
    "right",
    "management"
) + supportedOtherInterfaces
val maxInterfacesSupported = supportedInterfaceNames.size

internal fun addPortTupleToServiceInstance(schema: Schema): WorkflowDefinition {
    val workflowName = "Add port tuple to service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("addPortTupleToServiceInstance") {
        description = schema.createWorkflowDescription<ServiceInstance, PortTuple>()
        parameter("name", string) {
            description = "${PortTuple::class.java.allCapitalized} name"
            mandatory = true
        }
        parameter(parent, reference<ServiceInstance>()) {
            description = ServiceInstance::class.java.allCapitalized
            mandatory = true
        }
        step("Interfaces") {
            visibility = WhenNonNull(parent)
            (0 until maxInterfacesSupported).forEach {
                generatePortInput(it)
            }
        }
    }
}

private fun ParameterAggregator.generatePortInput(index: Int) {
    val interfaceName = supportedInterfaceNames[index]
    parameter("port$index", reference<VirtualMachineInterface>()) {
        description = interfaceName.capitalize()
        listedBy = actionCallTo(getPortsForServiceInterface).parameters(parent).string(interfaceName)
        visibility = FromActionVisibility(
            actionCallTo(serviceHasInterfaceWithName).parameter(parent).string(interfaceName)
        )
        mandatory = true
    }
}

internal fun deletePortTuple(schema: Schema): WorkflowDefinition {
    val workflowName = "Delete port tuple"

    return customWorkflow<PortTuple>(workflowName).withScriptFile("deletePortTuple") {
        parameter("portTuple", reference<PortTuple>()) {
            description = PortTuple::class.java.allCapitalized
            mandatory = true
        }
    }
}

internal fun removePortTuplefromServiceInstance(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove port tuple from service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("deletePortTuple") {
        parameter(parent, reference<ServiceInstance>()) {
            description = ServiceInstance::class.java.allCapitalized
            mandatory = true
        }
        parameter("portTuple", reference<PortTuple>()) {
            description = PortTuple::class.java.allCapitalized
            visibility = WhenNonNull(parent)
            listedBy = actionCallTo(getPortTuplesOfServiceInstance).parameter(parent)
            mandatory = true
        }
    }

}