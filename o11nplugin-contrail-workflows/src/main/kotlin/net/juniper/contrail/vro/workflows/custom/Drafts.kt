package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.Drafts
import net.juniper.contrail.vro.config.constants.commitDraftsInProjectWorkflowName
import net.juniper.contrail.vro.config.constants.discardDraftsInProjectWorkflowName
import net.juniper.contrail.vro.config.constants.commitGlobalDraftsWorkflowName
import net.juniper.contrail.vro.config.constants.discardGlobalDraftsWorkflowName
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.dsl.inCategory
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.reference

internal fun commitDraftsInProject(): WorkflowDefinition {
    val workflowName = commitDraftsInProjectWorkflowName

    return workflow(workflowName).withScriptFile("commitDraftsInProject") {
        parameter(item, reference<Project>()) {
            description = "Project to commit drafts in"
            mandatory = true
        }
    }.inCategory(Drafts)
}

internal fun discardDraftsInProject(): WorkflowDefinition {
    val workflowName = discardDraftsInProjectWorkflowName

    return workflow(workflowName).withScriptFile("discardDraftsInProject") {
        parameter(item, reference<Project>()) {
            description = "Project to discard drafts in"
            mandatory = true
        }
    }.inCategory(Drafts)
}

internal fun commitGlobalDrafts(): WorkflowDefinition {
    val workflowName = commitGlobalDraftsWorkflowName

    return workflow(workflowName).withScriptFile("commitGlobalDrafts") {
        parameter(item, Connection.reference) {
            description = "Contrail instance to commit global drafts in"
            mandatory = true
            dataBinding = fromAction(defaultConnection, type) {}
        }
    }.inCategory(Drafts)
}

internal fun discardGlobalDrafts(): WorkflowDefinition {
    val workflowName = discardGlobalDraftsWorkflowName

    return workflow(workflowName).withScriptFile("discardGlobalDrafts") {
        parameter(item, Connection.reference) {
            description = "Contrail instance to discard global drafts in"
            mandatory = true
            dataBinding = fromAction(defaultConnection, type) {}
        }
    }.inCategory(Drafts)
}