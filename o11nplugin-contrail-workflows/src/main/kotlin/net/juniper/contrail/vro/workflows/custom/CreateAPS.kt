package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.Project
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

        workflowInvocation(apsCreationWorkflow, mainMenu, "Create application policy set in project") {
            inputBind("parent", theProject)
            outputBind("item", resultAps)
        }
        choice(mainMenu, endId, "What would you like to do next?") {
            option("Finish", endId)
            option("Add new firewall policy", newFirewallPolicy)
            option("Add existing firewall policy", addPolicy)
            option("Add tag", addTag)
        }
        workflowInvocation(newFirewallPolicy, addRuleMenu, "Create firewall policy in project") {
            inputBind("parent", theProject)
            outputBind("item", resultFirewallPolicy)
        }
        choice(addRuleMenu, addNewPolicy, "Would you like to add another rule?") {
            option("Yes", createRule)
            option("No", addNewPolicy)
        }
        workflowInvocation(createRule, addRule, "Create firewall rule in project") {
            inputBind("parent", theProject)
            outputBind("item", resultFirewallRule)
        }
        workflowInvocation(addRule, addRuleMenu, "Add firewall rule to firewall policy") {
            inputBind("item", resultFirewallPolicy)
            inputBind("child", resultFirewallRule)
        }
        workflowInvocation(addNewPolicy, mainMenu, "Add firewall policy to application policy set") {
            inputBind("item", resultAps)
            inputBind("child", resultFirewallPolicy)
        }
        workflowInvocation(addPolicy, mainMenu, "Add firewall policy to application policy set") {
            inputBind("item", resultAps)
        }
        workflowInvocation(addTag, mainMenu, "Add tag to application policy set") {
            inputBind("item", resultAps)
        }
    }