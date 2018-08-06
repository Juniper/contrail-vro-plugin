/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.parentConnection
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation

internal fun addFirewallPolicyToAPS(schema: Schema): WorkflowDefinition {

    val workflowName = addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()

    return customWorkflow<ApplicationPolicySet>(workflowName).withScriptFile("addFirewallPolicyToAPS") {
        parameter(item, reference<ApplicationPolicySet>()) {
            description = schema.parentDescriptionInCreateRelation<ApplicationPolicySet, FirewallPolicy>()
            mandatory = true
            browserRoot = child.parentConnection
        }
        parameter(child, reference<FirewallPolicy>()) {
            description = schema.childDescriptionInCreateRelation<ApplicationPolicySet, FirewallPolicy>()
            mandatory = true
            validWhen = matchesSecurityScope(item, false)
            browserRoot = item.parentConnection
        }
    }
}

internal fun addTagToAPS(schema: Schema): WorkflowDefinition {

    val workflowName = addRelationWorkflowName<ApplicationPolicySet, Tag>()

    return customWorkflow<ApplicationPolicySet>(workflowName).withScriptFile("addFirewallPolicyToAPS") {
        parameter(item, reference<ApplicationPolicySet>()) {
            description = "Application Policy Set to add the tag to"
            mandatory = true
            browserRoot = child.parentConnection
        }
        parameter(child, reference<Tag>()) {
            description = "Tag to be added"
            mandatory = true
            browserRoot = item.parentConnection
            visibility = WhenNonNull(item)
            validWhen = matchesSecurityScope(item, false)
            listedBy = actionCallTo(net.juniper.contrail.vro.config.listTagsOfType).parameter(item).string("application")
        }
    }

}