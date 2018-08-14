/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.isA
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.custom.isNotReferencedBy
import net.juniper.contrail.vro.workflows.custom.matchesSecurityScope
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.and
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.dsl.parentConnection
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.childDescriptionInRemoveRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInRemoveRelation
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName

fun addReferenceWorkflow(relation: ForwardRelation, schema: Schema, config: Config): WorkflowDefinition {

    val parentClass = relation.declaredParentClass
    val childClass = relation.declaredChildClass
    val workflowName = addRelationWorkflowName(parentClass, childClass)
    val scriptBody = relation.addReferenceRelationScriptBody()
    val directValidation = parentClass.isA<Project>()

    return workflow(workflowName).withScript(scriptBody) {
        parameter(item, parentClass.reference) {
            description = schema.parentDescriptionInCreateRelation(parentClass, childClass)
            mandatory = true
            browserRoot = child.parentConnection
        }

        parameter(child, childClass.reference) {
            description = schema.childDescriptionInCreateRelation(parentClass, childClass, ignoreMissing = true)
            mandatory = true
            browserRoot = item.parentConnection
            validWhen = if (config.needsSecurityScopeValidation(childClass))
                matchesSecurityScope(item, directValidation) and isNotReferencedBy(item)
            else
                isNotReferencedBy(item)
        }
    }
}

fun removeReferenceWorkflow(relation: ForwardRelation): WorkflowDefinition {

    val parentClass = relation.declaredParentClass
    val childClass = relation.declaredChildClass
    val workflowName = removeRelationWorkflowName(parentClass, childClass)
    val scriptBody = relation.removeReferenceRelationScriptBody()

    return workflow(workflowName).withScript(scriptBody) {
        parameter(item, parentClass.reference) {
            description = parentDescriptionInRemoveRelation(parentClass, childClass)
            mandatory = true
        }
        parameter(child, childClass.reference) {
            description = childDescriptionInRemoveRelation(parentClass, childClass)
            mandatory = true
            visibility = WhenNonNull(item)
            browserRoot = item.asBrowserRoot()
            listedBy = actionCallTo(propertyValue).parameter(item).string(relation.pluginGetter)
        }
    }
}

private fun ForwardRelation.addReferenceRelationScriptBody() = """
$item.add${if (isReversed) parentPluginName else childPluginName}($child);
$item.update();
""".trim()

private fun ForwardRelation.removeReferenceRelationScriptBody() = """
$item.remove${if (isReversed) parentPluginName else childPluginName}($child);
$item.update();
""".trim()