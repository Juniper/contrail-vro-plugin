package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.constants.basePackageName
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.endId
import net.juniper.contrail.vro.workflows.dsl.withComplexParameters
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.reference

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
    workflow("Create APS").withComplexParameters(apsCreationWorkflow, workflowDefinitions) {
        attribute(resultAps, reference<ApplicationPolicySet>())
        attribute(resultFirewallPolicy, reference<FirewallPolicy>())
        attribute(resultFirewallRule, reference<FirewallRule>())
        attribute(theProject, reference<Project>())

        workflowInvokation(apsCreationWorkflow, basePackageName, "Create application policy set in project", mainMenu, mapOf(
            Pair("parent", theProject)
        ), mapOf(
            Pair("item", resultAps)))
        choice(mainMenu, "What would you like to do next?", listOf(
            Pair("Finish", endId),
            Pair("Add new firewall policy", newFirewallPolicy),
            Pair("Add existing firewall policy", addPolicy),
            Pair("Add tag", addTag)
        ), endId)
        workflowInvokation(newFirewallPolicy, basePackageName, "Create firewall policy in project", addRuleMenu, mapOf(
            Pair("parent", theProject)
        ), mapOf(
            Pair("item", resultFirewallPolicy)))
        choice(addRuleMenu, "Would you like to add another rule?", listOf(
            Pair("Yes", createRule),
            Pair("No", addNewPolicy)
        ), addNewPolicy)
        workflowInvokation(createRule, basePackageName, "Create firewall rule in project", addRule, mapOf(
            Pair("parent", theProject)
        ), mapOf(
            Pair("rule", resultFirewallRule)))
        workflowInvokation(addRule, basePackageName, "Add firewall rule to firewall policy", addRuleMenu, mapOf(
            Pair("item", resultFirewallPolicy),
            Pair("child", resultFirewallRule)
        ), mapOf())
        workflowInvokation(addNewPolicy, basePackageName, "Add firewall policy to application policy set", mainMenu, mapOf(
            Pair("item", resultAps),
            Pair("child", resultFirewallPolicy)
        ), mapOf())
        workflowInvokation(addPolicy, basePackageName, "Add firewall policy to application policy set", mainMenu, mapOf(
            Pair("item", resultAps)
        ), mapOf())
        workflowInvokation(addTag, basePackageName, "Add tag to application policy set", mainMenu, mapOf(
            Pair("item", resultAps)
        ), mapOf())
    }