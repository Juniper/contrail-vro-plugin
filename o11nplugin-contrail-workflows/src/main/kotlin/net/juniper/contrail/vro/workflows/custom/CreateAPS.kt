/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.createWorkflowName

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
val theProject = "projectAttribute"
val resultFirewallPolicy = "resultFirewallPolicy"
val resultFirewallRule = "resultFirewallRule"

internal fun createAPS(workflowDefinitions: List<WorkflowDefinition>): WorkflowDefinition =
    workflow(createWorkflowName<ApplicationPolicySet, FirewallPolicy>(hasRootParents = true)).withComplexParameters(apsCreationWorkflow, workflowDefinitions) {
        //create new user interaction which asks for projectAttribute

        attribute(resultAps, reference<ApplicationPolicySet>())
        attribute(resultFirewallPolicy, reference<FirewallPolicy>())
        attribute(resultFirewallRule, reference<FirewallRule>())
        attribute(theProject, reference<Project>())

        workflowInvocation(apsCreationWorkflow, mainMenu, createWorkflowName<Project, ApplicationPolicySet>(parentsInModel = 2)) {
            inputBind("parent", theProject)
            outputBind("item", resultAps)
        }
        choice(mainMenu, workflowEndItemId, "What would you like to do next?") {
            option("Finish", workflowEndItemId)
            option("Add new firewall policy", newFirewallPolicy)
            option("Add existing firewall policy", addPolicy)
            option("Add tag", addTag)
        }
        workflowInvocation(newFirewallPolicy, addRuleMenu, createWorkflowName<Project, FirewallPolicy>(parentsInModel = 2)) {
            inputBind("parent", theProject)
            outputBind("item", resultFirewallPolicy)
        }
        choice(addRuleMenu, addNewPolicy, "Would you like to add another rule?") {
            option("Yes", createRule)
            option("No", addNewPolicy)
        }
        workflowInvocation(createRule, addRule, createWorkflowName<Project, FirewallRule>(parentsInModel = 2)) {
            inputBind("parent", theProject)
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