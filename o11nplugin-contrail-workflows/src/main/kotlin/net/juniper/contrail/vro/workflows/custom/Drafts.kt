package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.Drafts
import net.juniper.contrail.vro.config.constants.commitDraftsInProject
import net.juniper.contrail.vro.config.constants.discardDraftsInProject
import net.juniper.contrail.vro.config.constants.commitGlobalDrafts
import net.juniper.contrail.vro.config.constants.discardGlobalDrafts
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.dsl.inCategory
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.reference

internal fun commitDraftsInProject(): WorkflowDefinition {
    val workflowName = commitDraftsInProject

    return workflow(workflowName).withScriptFile("commitDraftsInProject") {
        parameter(item, reference<Project>()) {
            description = "Project to commit drafts in"
            mandatory = true
        }
    }.inCategory(Drafts)
}

internal fun discardDraftsInProject(): WorkflowDefinition {
    val workflowName = discardDraftsInProject

    return workflow(workflowName).withScriptFile("discardDraftsInProject") {
        parameter(item, reference<Project>()) {
            description = "Project to discard drafts in"
            mandatory = true
        }
    }.inCategory(Drafts)
}

internal fun commitGlobalDrafts(): WorkflowDefinition {
    val workflowName = commitGlobalDrafts

    return workflow(workflowName).withScriptFile("commitGlobalDrafts") {
        parameter(item, Connection.reference) {
            description = "Contrail instance to commit global drafts in"
            mandatory = true
            dataBinding = fromAction(defaultConnection, type) {}
        }
    }.inCategory(Drafts)
}

internal fun discardGlobalDrafts(): WorkflowDefinition {
    val workflowName = discardGlobalDrafts

    return workflow(workflowName).withScriptFile("discardGlobalDrafts") {
        parameter(item, Connection.reference) {
            description = "Contrail instance to discard global drafts in"
            mandatory = true
            dataBinding = fromAction(defaultConnection, type) {}
        }
    }.inCategory(Drafts)
}