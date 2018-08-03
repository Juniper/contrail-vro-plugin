/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.constants.createGlobalApplicationPolicySetWithFirewallPoliciesWorkflowName
import net.juniper.contrail.vro.config.constants.createGlobalApplicationPolicySetWorkflowName
import net.juniper.contrail.vro.config.constants.createGlobalFirewallPolicyWorkflowName
import net.juniper.contrail.vro.config.constants.createGlobalFirewallRuleWorkflowName
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName

internal fun createGlobalAPS(workflowDefinitions: List<WorkflowDefinition>): WorkflowDefinition {
    val apsCreationWorkflow = 1
    val mainMenu = 2
    val newFirewallPolicy = 3
    val addRuleMenu = 4
    val createRule = 5
    val addRule = 6
    val addNewPolicy = 7
    val addPolicy = 8
    val addTag = 9

    val resultAps = "resultAps"
    val resultFirewallPolicy = "resultFirewallPolicy"
    val resultFirewallRule = "resultFirewallRule"

    return workflow(createGlobalApplicationPolicySetWithFirewallPoliciesWorkflowName).withComplexParameters(apsCreationWorkflow, workflowDefinitions) {
        attribute(resultAps, reference<ApplicationPolicySet>())
        attribute(resultFirewallPolicy, reference<FirewallPolicy>())
        attribute(resultFirewallRule, reference<FirewallRule>())

        workflowInvocation(apsCreationWorkflow, mainMenu, createGlobalApplicationPolicySetWorkflowName) {
            outputBind("item", resultAps)
        }
        choice(mainMenu, workflowEndItemId, "What would you like to do next?") {
            option("Finish", workflowEndItemId)
            option("Add new firewall policy", newFirewallPolicy)
            option("Add existing firewall policy", addPolicy)
            option("Add tag", addTag)
        }
        workflowInvocation(newFirewallPolicy, addRuleMenu, createGlobalFirewallPolicyWorkflowName) {
            outputBind("item", resultFirewallPolicy)
        }
        choice(addRuleMenu, addNewPolicy, "Would you like to add another rule?") {
            option("Yes", createRule)
            option("No", addNewPolicy)
        }
        workflowInvocation(createRule, addRule, createGlobalFirewallRuleWorkflowName) {
            outputBind("rule", resultFirewallRule)
        }
        workflowInvocation(addRule, addRuleMenu, addRelationWorkflowName<FirewallPolicy, FirewallRule>()) {
            inputBind("item", resultFirewallPolicy)
            inputBind("child", resultFirewallRule)
        }
        workflowInvocation(addNewPolicy, mainMenu, addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
            inputBind("child", resultFirewallPolicy)
        }
        workflowInvocation(addPolicy, mainMenu, addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(addTag, mainMenu, addRelationWorkflowName<ApplicationPolicySet, Tag>()) {
            inputBind("item", resultAps)
        }
    }
}