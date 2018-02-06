/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.WhenNonNull
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.relationDescription

fun addReferenceWorkflow(relation: ForwardRelation, schema: Schema): WorkflowDefinition {

    val parentName = relation.parentPluginName
    val childName = relation.childPluginName
    val workflowName = "Add ${childName.allLowerCase} to ${parentName.allLowerCase}"
    val scriptBody = relation.addReferenceRelationScriptBody()

    return workflow(workflowName).withScript(scriptBody) {
        parameter(parent, parentName.reference) {
            description = "${parentName.allCapitalized} to add to"
            mandatory = true
        }
        parameter(child, childName.reference) {
            description = schema.descriptionInCreateRelationWorkflow(relation.parentClass, relation.childClass)
            mandatory = true
        }
        if ( ! relation.simpleReference) {
            addProperties(relation.attribute, schema)
        }
    }
}

fun removeReferenceWorkflow(relation: ForwardRelation, action: Action): WorkflowDefinition {

    val parentName = relation.parentPluginName
    val childName = relation.childPluginName
    val workflowName = "Remove ${childName.allLowerCase} from ${parentName.allLowerCase}"
    val scriptBody = relation.removeReferenceRelationScriptBody()

    return workflow(workflowName).withScript(scriptBody) {
        parameter(parent, parentName.reference) {
            description = "${parentName.allCapitalized} to remove from"
            mandatory = true
        }
        parameter(child, childName.reference) {
            description = "${childName.allCapitalized} to be removed"
            mandatory = true
            visibility = WhenNonNull(parent)
            listedBy(action)
        }
    }
}

private fun Schema.descriptionInCreateRelationWorkflow(parentClazz: ObjectClass, clazz: ObjectClass) = """
${clazz.allCapitalized} to be added
${relationDescription(parentClazz, clazz)}
""".trim()

private fun ForwardRelation.addReferenceRelationScriptBody() =
    if (simpleReference)
        addSimpleReferenceRelationScriptBody()
    else
        addRelationWithAttributeScriptBody()

private fun ForwardRelation.addRelationWithAttributeScriptBody() = """
var attribute = new Contrail${attribute.pluginName}();
${ attribute.attributeCode("attribute") }
$parent.add$childPluginName($child, attribute);
$retrieveExecutorAndUpdateParent
"""

private fun ForwardRelation.addSimpleReferenceRelationScriptBody() = """
$parent.add$childPluginName($child);
$retrieveExecutorAndUpdateParent
"""

private fun ForwardRelation.removeReferenceRelationScriptBody() = """
${if (simpleReference)
    "$parent.remove$childPluginName($child);"
else
    "$parent.remove$childName($child, null);"
}
$retrieveExecutorAndUpdateParent
""".trimIndent()