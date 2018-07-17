/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.ServiceInstanceType
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.maxInterfacesSupported
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.constants.supportedInterfaceNames
import net.juniper.contrail.vro.config.templateHasInterfaceWithName
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.dsl.asVisibilityCondition
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.createSimpleWorkflowName
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

private val serviceTemplate = "serviceTemplate"

internal fun createServiceInstance(schema: Schema) : WorkflowDefinition =
    customWorkflow<ServiceInstance>(createSimpleWorkflowName<ServiceInstance>()).withScriptFile("createServiceInstance") {
        description = relationDescription<Project, ServiceInstance>(schema)
        parameter(parent, reference<Project>()) {
            description = "Project this service instance will belong to"
            mandatory = true
        }
        parameter("name", string) {
            description = "Name of the service instance"
            mandatory = true
        }
        parameter(serviceTemplate, reference<ServiceTemplate>()) {
            description = relationDescription<ServiceInstance, ServiceTemplate>(schema)
            mandatory = true
        }
        parameter("virtualRouterId", string) {
            description = propertyDescription<ServiceInstanceType>(schema)
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

private fun ParameterAggregator.generateServiceInstanceInterface(index: Int) {
    val interfaceName = supportedInterfaceNames[index]
    parameter("interface$index", reference<VirtualNetwork>()) {
        description = interfaceName.capitalize()
        visibility = actionCallTo(templateHasInterfaceWithName)
            .parameter(serviceTemplate).string(interfaceName).asVisibilityCondition()
        mandatory = true
        browserRoot = parent.asBrowserRoot()
    }
}