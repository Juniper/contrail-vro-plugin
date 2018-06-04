/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.TagType
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.name
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

internal fun createTagType(): WorkflowDefinition {

    val workflowName = "Create tag type"

    return customWorkflow<TagType>(workflowName).withScriptFile("createTagType") {
        parameter(name, string) {
            description = "Name of the tag type"
            mandatory = true
        }
        parameter(parent, Connection.reference) {
            description = "Parent connection"
            mandatory = true
        }
        output(item, reference<TagType>()) {
            description = "Tag Type created in this workflow"
        }
    }
}

internal fun deleteTagType(): WorkflowDefinition {

    val workflowName = "Delete tag type"

    return customWorkflow<TagType>(workflowName).withScriptFile("deleteTagType") {
        parameter(item, reference<TagType>()) {
            description = "Tag Type to delete"
            mandatory = true
        }
    }
}