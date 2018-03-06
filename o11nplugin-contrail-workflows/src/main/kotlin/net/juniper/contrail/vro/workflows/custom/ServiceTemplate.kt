/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Domain
import net.juniper.contrail.api.types.ServiceApplianceSet
import net.juniper.contrail.api.types.ServiceTemplateType
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.extractPredefinedAnswers
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.workflows.schema.relationDescription

val supportedVersion : Long = 2
val supportedVersions = listOf(supportedVersion)

internal fun createServiceTemplate(schema: Schema) : WorkflowDefinition {
    val workflowName = "Create service template"

    return customWorkflow<ServiceTemplate>(workflowName).withScriptFile("createServiceTemplate") {
        description = schema.relationDescription<Domain, ServiceTemplate>()
        parameter(parent, Connection.reference) {
            description = "Parent connection"
            mandatory = true
        }
        parameter("name", string) {
            description = "Name\nName of service template"
            mandatory = true
        }
        parameter("version", number) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            predefinedAnswers = supportedVersions
            min = supportedVersion
            max = supportedVersion
        }
        parameter("serviceMode", string) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            extractPredefinedAnswers<ServiceTemplateType>(schema)
        }
        parameter("serviceType", string) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            extractPredefinedAnswers<ServiceTemplateType>(schema)
        }
        parameter("serviceVirtualizationType", string) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = false
            extractPredefinedAnswers<ServiceTemplateType>(schema)
        }
        parameter("interfaceType", string.array) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = true
            predefinedAnswers = supportedInterfaceNames
            sameValues = false
        }
        parameter("serviceApplianceSet", reference<ServiceApplianceSet>().array) {
            extractRelationDescription<ServiceTemplate, ServiceApplianceSet>(schema)
            mandatory = false
        }
        parameter("vrouterInstanceType", string) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = false
            extractPredefinedAnswers<ServiceTemplateType>(schema)
        }
        parameter("availabilityZoneEnable", boolean) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = false
            defaultValue = false
        }
        parameter("instanceData", string) {
            extractPropertyDescription<ServiceTemplateType>(schema)
            mandatory = false
            multiline = true
        }
        output(item, reference<ServiceTemplate>()) {
            description = "Service template created in this workflow"
        }
    }
}