/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Domain
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.api.types.ServiceTemplateType
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.constants.supportedInterfaceNames
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.createSimpleWorkflowName
import net.juniper.contrail.vro.workflows.util.extractPredefinedAnswers
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

val supportedVersion : Long = 2
val supportedVersions = listOf(supportedVersion)
val serviceTypes = listOf("firewall", "analyzer")
val serviceVirtualizationTypes = listOf("virtual-machine", "physical-device")

internal fun createServiceTemplate(schema: Schema) : WorkflowDefinition =
    customWorkflow<ServiceTemplate>(createSimpleWorkflowName<ServiceTemplate>()).withScriptFile("createServiceTemplate") {
        description = relationDescription<Domain, ServiceTemplate>(schema)
        parameter(parent, Connection.reference) {
            description = "Parent connection"
            mandatory = true
            dataBinding = fromAction(defaultConnection, type) {}
        }
        parameter("name", string) {
            description = "Name\nName of service template"
            mandatory = true
        }
        parameter("version", number) {
            description = propertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            predefinedAnswers = supportedVersions
            min = supportedVersion
            max = supportedVersion
        }
        parameter("serviceMode", string) {
            description = propertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            extractPredefinedAnswers<ServiceTemplateType>(schema)
        }
        parameter("serviceType", string) {
            description = propertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            predefinedAnswers = serviceTypes
        }
        parameter("serviceVirtualizationType", string) {
            description = propertyDescription<ServiceTemplateType>(schema)
            mandatory = false
            predefinedAnswers = serviceVirtualizationTypes
        }
        parameter("interfaceType", string.array) {
            description = propertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            predefinedAnswers = supportedInterfaceNames
            sameValues = false
        }
        output(item, reference<ServiceTemplate>()) {
            description = "Service template created in this workflow"
        }
    }
