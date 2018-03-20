/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.allLowerCase
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.parameterName
import net.juniper.contrail.vro.config.parentConnection
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.refPropertyName
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation

fun addReferenceWorkflow(relation: ForwardRelation, schema: Schema): WorkflowDefinition {

    val parent = relation.parentClass
    val child = relation.childClass
    val childReferenceName = relation.child
    val workflowName = schema.addRelationWorkflowName(parent, child)
    val scriptBody = relation.addReferenceRelationScriptBody()

    return workflow(workflowName).withScript(scriptBody) {
        parameter(item, parent.reference) {
            description = schema.parentDescriptionInCreateRelation(parent, child)
            mandatory = true
            browserRoot = actionCallTo(parentConnection).parameter(childReferenceName).asBrowserRoot()
        }

        parameter(childReferenceName, child.reference) {
            description = schema.childDescriptionInCreateRelation(parent, child)
            mandatory = true
            browserRoot = actionCallTo(parentConnection).parameter(item).asBrowserRoot()
        }
    }
}

fun removeReferenceWorkflow(relation: ForwardRelation): WorkflowDefinition {

    val parentName = relation.parentPluginName
    val childName = relation.childPluginName
    val workflowName = "Remove ${childName.allLowerCase} from ${parentName.allLowerCase}"
    val scriptBody = relation.removeReferenceRelationScriptBody()

    return workflow(workflowName).withScript(scriptBody) {
        parameter(item, parentName.reference) {
            description = "${parentName.allCapitalized} to remove ${childName.allCapitalized} from"
            mandatory = true
        }
        parameter(relation.child, childName.reference) {
            description = "${childName.allCapitalized} to be removed"
            mandatory = true
            visibility = WhenNonNull(item)
            browserRoot = item.asBrowserRoot()
            listedBy = actionCallTo(propertyValue).parameter(item).string(relation.childClass.refPropertyName)
        }
    }
}

private fun ForwardRelation.addReferenceRelationScriptBody() =
    if (simpleReference)
        addSimpleReferenceRelationScriptBody()
    else
        addRelationWithAttributeScriptBody()

private fun ForwardRelation.addRelationWithAttributeScriptBody() = """
$item.add$childPluginName($child, null);
$item.update();
""".trim()

private fun ForwardRelation.addSimpleReferenceRelationScriptBody() = """
$item.add$childPluginName($child);
$item.update();
""".trim()

private val ForwardRelation.child get() =
    childClass.pluginName.parameterName

private fun ForwardRelation.removeReferenceRelationScriptBody() = """
${if (simpleReference)
    "$item.remove$childPluginName($child);"
else
    "$item.remove$childName($child, null);"
}
$item.update();
""".trimIndent()