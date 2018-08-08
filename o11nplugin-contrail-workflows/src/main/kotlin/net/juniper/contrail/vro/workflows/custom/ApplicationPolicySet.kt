/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
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
            browserRoot = item.parentConnection
            validWhen = matchesSecurityScope(item, false) and isNotReferencedBy(item)

        }
    }
}