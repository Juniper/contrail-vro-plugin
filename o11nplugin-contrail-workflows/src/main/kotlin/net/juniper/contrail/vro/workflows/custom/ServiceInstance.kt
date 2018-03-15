/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceInstanceType
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.ServiceInstanceInterfaceType
import net.juniper.contrail.api.types.AllowedAddressPair
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.maxInterfacesSupported
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.constants.supportedInterfaceNames
import net.juniper.contrail.vro.config.templateHasInterfaceWithName
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.FromActionVisibility
import net.juniper.contrail.vro.config.getServiceInstanceInterfaceNames
import net.juniper.contrail.vro.config.getAllowedAddressPairs
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.relationDescription
import net.juniper.contrail.vro.workflows.util.extractPredefinedAnswers
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

private val serviceTemplate = "serviceTemplate"

internal fun createServiceInstance(schema: Schema) : WorkflowDefinition {
    val workflowName = "Create service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("createServiceInstance") {
        description = schema.relationDescription<Project, ServiceInstance>()
        parameter(parent, reference<Project>()) {
            description = "Project this service instance will belong to"
            mandatory = true
        }
        parameter("name", string) {
            description = "Name of the service instance"
            mandatory = true
        }
        parameter(serviceTemplate, reference<ServiceTemplate>()) {
            extractRelationDescription<ServiceInstance, ServiceTemplate>(schema)
            mandatory = true
        }
        parameter("virtualRouterId", string) {
            extractPropertyDescription<ServiceInstanceType>(schema)
            mandatory = false
        }
        step("Interfaces") {
            description = schema.propertyDescription<ServiceInstanceType>("interfaceList")
            visibility = WhenNonNull(serviceTemplate)
            (0 until maxInterfacesSupported).forEach {
                generateServiceInstanceInterface(it)
            }
        }
        output(item, reference<ServiceInstance>()) {
            description = "Service instance created in this workflow"
        }
    }
}

internal fun addAllowedAddressPair(schema: Schema) : WorkflowDefinition {
    val workflowName = "Add allowed address pair to service instance"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("addAllowedAddressPair") {
        parameter(item, reference<ServiceInstance>()) {
            description = "Service instance to add allowed address pair to"
            mandatory = true
        }
        step("Properties") {
            visibility = WhenNonNull(item)
            description = schema.propertyDescription<ServiceInstanceInterfaceType>("allowedAddressPairs")
            parameter("interfaceName", string) {
                description = "Interface to add allowed address pair to"
                mandatory = true
                predefinedAnswersFrom = actionCallTo(getServiceInstanceInterfaceNames).parameter(item)
            }
            parameter("ip", string) {
                description = "CIDR"
                mandatory = true
                validWhen = isCidr()
            }
            parameter("mac", string) {
                description = "MAC"
                mandatory = false
                validWhen = isMac()
            }
            parameter("addressMode", string) {
                extractPropertyDescription<AllowedAddressPair>(schema)
                mandatory = false
                extractPredefinedAnswers<AllowedAddressPair>(schema)
            }
        }
    }
}

internal fun removeAllowedAddressPair(schema: Schema) : WorkflowDefinition {
    val workflowName = "Remove allowed address pair from service instance"
    val interfaceName = "interfaceName"

    return customWorkflow<ServiceInstance>(workflowName).withScriptFile("removeAllowedAddressPair") {
        parameter(item, reference<ServiceInstance>()) {
            description = "Service instance to remove allowed address pair from"
            mandatory = true
        }
        step("Properties") {
            visibility = WhenNonNull(item)
            parameter(interfaceName, string) {
                description = "Interface from which allowed address pair should be removed"
                mandatory = true
                predefinedAnswersFrom = actionCallTo(getServiceInstanceInterfaceNames).parameter(item)
            }
            parameter("allowedAddressPair", string) {
                description = "Allowed address pair to be removed"
                mandatory = true
                visibility = WhenNonNull(interfaceName)
                predefinedAnswersFrom = actionCallTo(getAllowedAddressPairs).parameters(item, interfaceName)
            }
        }
    }
}

private fun ParameterAggregator.generateServiceInstanceInterface(index: Int) {
    val interfaceName = supportedInterfaceNames[index]
    parameter("interface$index", reference<VirtualNetwork>()) {
        description = interfaceName.capitalize()
        visibility = FromActionVisibility(
            actionCallTo(templateHasInterfaceWithName).parameter(serviceTemplate).string(interfaceName)
        )
        mandatory = true
        browserRoot = parent.asBrowserRoot()
    }
}