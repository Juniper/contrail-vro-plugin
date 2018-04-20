/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.PortTuple
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.asChildRef
import net.juniper.contrail.vro.config.constants.maxInterfacesSupported
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.descriptionOf
import net.juniper.contrail.vro.config.portsForServiceInterface
import net.juniper.contrail.vro.config.serviceHasInterfaceWithName
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.constants.supportedInterfaceNames
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.asVisibilityCondition
import net.juniper.contrail.vro.schema.createWorkflowDescription

internal fun addPortTupleToServiceInstance(schema: Schema): WorkflowDefinition {
    val workflowName = "Add port tuple to service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("addPortTupleToServiceInstance") {
        description = schema.createWorkflowDescription<ServiceInstance, PortTuple>()
        parameter("name", string) {
            description = "${descriptionOf<PortTuple>()} name"
            mandatory = true
        }
        parameter(parent, reference<ServiceInstance>()) {
            description = descriptionOf<ServiceInstance>()
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
        description = "${interfaceName.capitalize()} Interface"
        listedBy = actionCallTo(portsForServiceInterface).parameters(parent).string(interfaceName)
        visibility = actionCallTo(serviceHasInterfaceWithName)
            .parameter(parent).string(interfaceName).asVisibilityCondition()
        mandatory = true
    }
}

internal fun removePortTupleFromServiceInstance(): WorkflowDefinition {
    val workflowName = "Remove port tuple from service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("removePortTupleFromServiceInstance") {
        parameter(parent, reference<ServiceInstance>()) {
            description = descriptionOf<ServiceInstance>()
            mandatory = true
        }
        parameter(item, reference<PortTuple>()) {
            description = descriptionOf<PortTuple>()
            visibility = WhenNonNull(parent)
            listedBy = actionCallTo(propertyValue).parameter(parent).string(asChildRef<PortTuple>())
            mandatory = true
        }
    }
}