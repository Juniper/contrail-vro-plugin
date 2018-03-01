/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.extractRelationDescription
import net.juniper.contrail.vro.config.getPortsForServiceInterface
import net.juniper.contrail.vro.config.serviceHasInterfaceWithName
import net.juniper.contrail.vro.workflows.dsl.FromActionVisibility

val maxOtherInterfacesSupported = 8
val supportedOtherInterfaces = (0 until maxOtherInterfacesSupported).map { "other$it" }
val supportedInterfaceNames = listOf(
    "left",
    "right",
    "management"
) + supportedOtherInterfaces
val maxInterfacesSupported = supportedInterfaceNames.size

internal fun addPortTupleToServiceInstance(schema: Schema): WorkflowDefinition {
    val workflowName = "Add Port tuple to service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("addPortTupleToServiceInstance") {
        parameter("name", string) {
            extractRelationDescription<ServiceInstance, PortTuple>(schema)
            mandatory = true
        }
        parameter(parent, reference<ServiceInstance>()) {
            extractRelationDescription<Project, ServiceInstance>(schema)
            mandatory = true
        }
        (0 until maxInterfacesSupported).forEach {
            generatePortInput(it)
        }
    }
}

private fun ParameterAggregator.generatePortInput(index: Int) {
    val interfaceName = supportedInterfaceNames[index]
    parameter("port$index", reference<VirtualMachineInterface>()) {
        description = interfaceName
        listedBy = actionCallTo(getPortsForServiceInterface).parameters(parent).string(interfaceName)
        visibility = FromActionVisibility(
            actionCallTo(serviceHasInterfaceWithName).parameter(parent).string(interfaceName)
        )
        mandatory = true
    }
}