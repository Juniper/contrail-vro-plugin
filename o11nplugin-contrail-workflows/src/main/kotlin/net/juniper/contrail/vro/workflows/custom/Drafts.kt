package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.constants.commitDraftsInProject
import net.juniper.contrail.vro.config.constants.discardDraftsInProject
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.reference

internal fun commitDraftsInProject(): WorkflowDefinition {
    val workflowName = commitDraftsInProject

    return customWorkflow<Project>(workflowName).withScriptFile("commitDraftsInProject") {
        parameter(item, reference<Project>()) {
            description = "Project to commit drafts in"
            mandatory = true
        }
    }
}

internal fun discardDraftsInProject(): WorkflowDefinition {
    val workflowName = discardDraftsInProject

    return customWorkflow<Project>(workflowName).withScriptFile("discardDraftsInProject") {
        parameter(item, reference<Project>()) {
            description = "Project to discard drafts in"
            mandatory = true
        }
    }
}