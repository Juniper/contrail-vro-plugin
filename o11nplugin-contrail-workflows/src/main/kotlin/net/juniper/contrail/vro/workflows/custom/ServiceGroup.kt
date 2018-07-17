/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.vro.config.constants.addServiceToServiceGroupWorkflowName
import net.juniper.contrail.vro.config.constants.editServiceOfServiceGroupWorkflowName
import net.juniper.contrail.vro.config.constants.removeServiceFromServiceGroupWorkflowName
import net.juniper.contrail.vro.config.constants.any
import net.juniper.contrail.vro.config.constants.icmp
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.tcp
import net.juniper.contrail.vro.config.constants.udp
import net.juniper.contrail.vro.config.serviceGroupServices
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

internal fun addServiceToServiceGroupWorkflow(): WorkflowDefinition =
    customWorkflow<ServiceGroup>(addServiceToServiceGroupWorkflowName).withScriptFile("addServiceToServiceGroup") {
        step("Service Group") {
            parameter(item, reference<ServiceGroup>()) {
                description = "Service Group to add service to"
                mandatory = true
            }
        }
        serviceGroupParameters(item, false)
    }

internal fun editServiceGroupServiceWorkflow(): WorkflowDefinition =
    customWorkflow<ServiceGroup>(editServiceOfServiceGroupWorkflowName).withScriptFile("editServiceOfServiceGroup") {
        step("Service Group") {
            parameter(item, reference<ServiceGroup>()) {
                description = "Service Group to edit service"
                mandatory = true
            }
            parameter(service, string) {
                visibility = WhenNonNull(item)
                description = "Service to edit"
                predefinedAnswersFrom = actionCallTo(serviceGroupServices).parameter(item)
            }
        }
        serviceGroupParameters(service, true)
    }

private fun PresentationParametersBuilder.serviceGroupParameters(visibilityParameter: String, editing: Boolean) {
    step("Service Properties") {
        visibility = WhenNonNull(visibilityParameter)
        parameter("protocol", string) {
            mandatory = true
            // There is no information about protocols in the schema, values were taken from the UI
            defaultValue = tcp
            predefinedAnswers = listOf(tcp, udp, icmp)
            if (editing) dataBinding = servicePropertyDataBinding()
        }
        parameter("port", string) {
            mandatory = true
            defaultValue = any
            if (editing) dataBinding = servicePropertyDataBinding()
        }
    }
}

internal fun removeServiceGroupServiceWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<ServiceGroup>(removeServiceFromServiceGroupWorkflowName).withScriptFile("removeServiceFromServiceGroup") {
        parameter(item, reference<ServiceGroup>()) {
            description = "Service Group to remove service from"
            mandatory = true
        }
        parameter(service, string) {
            visibility = WhenNonNull(item)
            description = "Service to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(serviceGroupServices).parameter(item)
        }
    }