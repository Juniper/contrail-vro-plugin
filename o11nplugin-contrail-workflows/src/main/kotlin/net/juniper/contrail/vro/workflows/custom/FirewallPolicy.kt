/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.and
import net.juniper.contrail.vro.workflows.dsl.parentConnection
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation

internal fun addFirewallRuleToFirewallPolicy(schema: Schema): WorkflowDefinition {

    val workflowName = addRelationWorkflowName<FirewallPolicy, FirewallRule>()

    return customWorkflow<FirewallPolicy>(workflowName).withScriptFile("addFirewallRuleToFirewallPolicy") {
        parameter(item, reference<FirewallPolicy>()) {
            description = schema.parentDescriptionInCreateRelation<FirewallPolicy, FirewallRule>()
            mandatory = true
            browserRoot = child.parentConnection
        }
        parameter(child, reference<FirewallRule>()) {
            description = schema.childDescriptionInCreateRelation<FirewallPolicy, FirewallRule>()
            mandatory = true
            validWhen = matchesSecurityScope(item, false)
            browserRoot = item.parentConnection
            validWhen = isNotReferencedBy(item) and matchesSecurityScope(item, false)
        }
    }
}