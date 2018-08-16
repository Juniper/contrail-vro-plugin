/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ConfigRoot
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.config.listTagTypes
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.createGlobalWorkflowName
import net.juniper.contrail.vro.workflows.util.createWorkflowName
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.relationDescription

internal fun createGlobalTag(schema: Schema) =
    createTag(schema, global = true)

internal fun createTagInProject(schema: Schema) =
    createTag(schema, global = false)

private fun createTag(schema: Schema, global: Boolean): WorkflowDefinition {

    val workflowName = if (global) createGlobalWorkflowName<Tag>() else createWorkflowName<Project, Tag>()
    val scriptName = workflowName.toScriptName()
    val parentName = if (global) Connection else Project::class.java.simpleName

    return customWorkflow<Tag>(workflowName).withScriptFile(scriptName) {
        description = relationDescription<ConfigRoot, Tag>(schema)
        parameter(parent, parentName.reference) {
            description = "Parent ${parentName.allCapitalized}"
            mandatory = true
            if (parentName == Connection)
                dataBinding = fromAction(defaultConnection, type) {}
        }
        parameter("typeName", string) {
            description = propertyDescription<Tag>(schema)
            predefinedAnswersFrom = actionCallTo(listTagTypes).parameter(parent)
            mandatory = true
        }
        parameter("value", string) {
            description = propertyDescription<Tag>(schema)
            mandatory = true
        }
    }
}