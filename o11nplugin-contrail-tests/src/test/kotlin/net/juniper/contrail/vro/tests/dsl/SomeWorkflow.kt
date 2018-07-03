/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.workflows.custom.apsCreationWorkflow
import net.juniper.contrail.vro.workflows.custom.resultAps
import net.juniper.contrail.vro.workflows.custom.resultFirewallPolicy
import net.juniper.contrail.vro.workflows.custom.resultFirewallRule
import net.juniper.contrail.vro.workflows.custom.theProject
import net.juniper.contrail.vro.workflows.dsl.END
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.reference

val start = 1
val ch1 = 2
val ch2 = 3

fun someComplexWorkflow() : WorkflowDefinition {
    val workflowDefinitions = mutableListOf<WorkflowDefinition>()
    return workflow("Create application policy set with firewall policies in project").withComplexParameters(apsCreationWorkflow, workflowDefinitions) {
        attribute(resultAps, reference<ApplicationPolicySet>())
        attribute(resultFirewallPolicy, reference<FirewallPolicy>())
        attribute(resultFirewallRule, reference<FirewallRule>())
        attribute(theProject, reference<Project>())

        choice(start, ch1, "Do you want to go back to start?") {
            option("Yes!", start)
            option("No", ch1)
        }

        choice(ch1, ch2, "Are you sure?") {
            option("Yes", ch2)
            option("No", start)
        }

        choice(ch2, END.id, "Exit?") {
            option("Yes", END.id)
            option("No", start)
        }
    }
}

fun someSimpleWorkflow() : WorkflowDefinition {
    return workflow("abcd").withScript("") {

    }
}