/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.constants.createApplicationPolicySetWithFirewallPoliciesInProjectWorkflowName
import net.juniper.contrail.vro.config.constants.createGlobalApplicationPolicySetWithFirewallPoliciesWorkflowName
import net.juniper.contrail.vro.config.constants.editFirewallRuleWorkflowName
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.createGlobalWorkflowName
import net.juniper.contrail.vro.workflows.util.createWorkflowName
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName

private val apsCreationWorkflow = 1
private val mainMenu = 2
private val newFirewallPolicy = 3
private val removeFirewallPolicy = 4
private val addRuleMenu = 5
private val createRule = 6
private val addRule = 7
private val removeRule = 8
private val editRule = 9
private val addNewPolicy = 10
private val addPolicy = 11
private val addTag = 12
private val removeTag = 13
private val inputItem = 14

private val resultAps = "resultAps"
private val theProject = "projectAttribute"
private val resultFirewallPolicy = "resultFirewallPolicy"
private val resultFirewallRule = "resultFirewallRule"

internal fun createAPSInProject(workflowDefinitions: List<WorkflowDefinition>): WorkflowDefinition =
    workflow(createApplicationPolicySetWithFirewallPoliciesInProjectWorkflowName).withComplexParameters(inputItem, workflowDefinitions) {
        attribute(resultAps, reference<ApplicationPolicySet>())
        attribute(resultFirewallPolicy, reference<FirewallPolicy>())
        attribute(resultFirewallRule, reference<FirewallRule>())
        attribute(theProject, reference<Project>())

        attributeInput(inputItem, apsCreationWorkflow) {
            parameter(theProject, reference<Project>()) {}
        }

        workflowInvocation(apsCreationWorkflow, mainMenu, createWorkflowName<Project, ApplicationPolicySet>()) {
            inputBind("parent", theProject)
            outputBind("item", resultAps)
        }
        choice(mainMenu, workflowEndItemId, "What would you like to do next?") {
            option("Finish", workflowEndItemId)
            option("Add new firewall policy", newFirewallPolicy)
            option("Add existing firewall policy", addPolicy)
            option("Remove firewall policy", removeFirewallPolicy)
            option("Add tag", addTag)
            option("Remove tag", removeTag)
        }
        workflowInvocation(newFirewallPolicy, addRuleMenu, createWorkflowName<Project, FirewallPolicy>()) {
            inputBind("parent", theProject)
            outputBind("item", resultFirewallPolicy)
        }
        choice(addRuleMenu, addNewPolicy, "What would you like to do next?") {
            option("Add firewall rule", createRule)
            option("Remove firewall rule", removeRule)
            option("Edit firewall rule", editRule)
            option("Submit firewall policy", addNewPolicy)
        }
        workflowInvocation(createRule, addRule, createWorkflowName<Project, FirewallRule>()) {
            inputBind("parent", theProject)
            outputBind("rule", resultFirewallRule)
        }
        workflowInvocation(addRule, addRuleMenu, addRelationWorkflowName<FirewallPolicy, FirewallRule>()) {
            inputBind("item", resultFirewallPolicy)
            inputBind("child", resultFirewallRule)
        }
        workflowInvocation(removeRule, addRuleMenu, removeRelationWorkflowName<FirewallPolicy, FirewallRule>()) {
            inputBind("item", resultFirewallPolicy)
        }
        workflowInvocation(editRule, addRuleMenu, editFirewallRuleWorkflowName) {}
        workflowInvocation(addNewPolicy, mainMenu, addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
            inputBind("child", resultFirewallPolicy)
        }
        workflowInvocation(removeFirewallPolicy, mainMenu, removeRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(addPolicy, mainMenu, addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(addTag, mainMenu, addRelationWorkflowName<ApplicationPolicySet, Tag>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(removeTag, mainMenu, removeRelationWorkflowName<ApplicationPolicySet, Tag>()) {
            inputBind("item", resultAps)
        }
        automaticWorkflowOutput {
            output(resultAps, "resultItem")
        }
    }

internal fun createGlobalAPS(workflowDefinitions: List<WorkflowDefinition>): WorkflowDefinition =
    workflow(createGlobalApplicationPolicySetWithFirewallPoliciesWorkflowName).withComplexParameters(apsCreationWorkflow, workflowDefinitions) {
        attribute(resultAps, reference<ApplicationPolicySet>())
        attribute(resultFirewallPolicy, reference<FirewallPolicy>())
        attribute(resultFirewallRule, reference<FirewallRule>())

        workflowInvocation(apsCreationWorkflow, mainMenu, createGlobalWorkflowName<ApplicationPolicySet>()) {
            outputBind("item", resultAps)
        }
        choice(mainMenu, workflowEndItemId, "What would you like to do next?") {
            option("Finish", workflowEndItemId)
            option("Add new firewall policy", newFirewallPolicy)
            option("Add existing firewall policy", addPolicy)
            option("Remove firewall policy", removeFirewallPolicy)
            option("Add tag", addTag)
            option("Remove tag", removeTag)

        }
        workflowInvocation(newFirewallPolicy, addRuleMenu, createGlobalWorkflowName<FirewallPolicy>()) {
            outputBind("item", resultFirewallPolicy)
        }
        choice(addRuleMenu, addNewPolicy, "What would you like to do next?") {
            option("Add firewall rule", createRule)
            option("Remove firewall rule", removeRule)
            option("Edit firewall rule", editRule)
            option("Submit firewall policy", addNewPolicy)
        }
        workflowInvocation(createRule, addRule, createGlobalWorkflowName<FirewallRule>()) {
            outputBind("rule", resultFirewallRule)
        }
        workflowInvocation(addRule, addRuleMenu, addRelationWorkflowName<FirewallPolicy, FirewallRule>()) {
            inputBind("item", resultFirewallPolicy)
            inputBind("child", resultFirewallRule)
        }
        workflowInvocation(removeRule, addRuleMenu, removeRelationWorkflowName<FirewallPolicy, FirewallRule>()) {
            inputBind("item", resultFirewallPolicy)
        }
        workflowInvocation(editRule, addRuleMenu, editFirewallRuleWorkflowName) {}
        workflowInvocation(addNewPolicy, mainMenu, addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
            inputBind("child", resultFirewallPolicy)
        }
        workflowInvocation(removeFirewallPolicy, mainMenu, removeRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(addPolicy, mainMenu, addRelationWorkflowName<ApplicationPolicySet, FirewallPolicy>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(addTag, mainMenu, addRelationWorkflowName<ApplicationPolicySet, Tag>()) {
            inputBind("item", resultAps)
        }
        workflowInvocation(removeTag, mainMenu, removeRelationWorkflowName<ApplicationPolicySet, Tag>()) {
            inputBind("item", resultAps)
        }
        automaticWorkflowOutput {
            output(resultAps, "resultItem")
        }
    }