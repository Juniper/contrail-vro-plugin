/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.bold
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.pluralParameterName
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.Reference
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.objectDescription
import net.juniper.contrail.vro.workflows.schema.relationDescription

fun createWorkflow(clazz: ObjectClass, parentClazz: ObjectClass?, refs: List<ObjectClass>, schema: Schema): WorkflowDefinition {

    val workflowName = "Create ${clazz.allLowerCase}"
    val parentName = parentClazz?.pluginName ?: Connection

    return workflow(workflowName).withScript(createScriptBody(clazz, parentClazz, refs)) {
        description = schema.createWorkflowDescription(clazz)
        parameter("name", string) {
            description = "${clazz.allCapitalized} name"
            mandatory = true

        }
        parameter(parent, parentName.reference) {
            description = "Parent ${parentName.allCapitalized}"
            mandatory = true

        }

        if (!refs.isEmpty()) {
            step("References") {
                for (ref in refs) {
                    parameter(ref.pluralParameterName, Reference(ref).array) {
                        description = schema.relationInCreateWorkflowDescription(clazz, ref)
                    }
                }
            }
        }

        addProperties(clazz, schema)
    }
}

fun deleteWorkflow(clazz: ObjectClass) =
    deleteWorkflow(clazz.pluginName, deleteScriptBody(clazz.pluginName))

private fun Schema.createWorkflowDescription(clazz: ObjectClass) : String? {
    val objectDescription = objectDescription(clazz) ?: return null
    return """
        ${clazz.allCapitalized.bold}
        $objectDescription
    """.trimIndent()
}
private fun Schema.relationInCreateWorkflowDescription(parentClazz: ObjectClass, clazz: ObjectClass) : String {
    val relationDescription = relationDescription(parentClazz, clazz)
    return """
        ${clazz.folderName.bold}
        $relationDescription
    """.trimIndent()
}

private fun createScriptBody(clazz: Class<*>, parentClazz: ObjectClass?, references: List<ObjectClass>) = """
${parent.retrieveExecutor}
var $item = new Contrail${clazz.pluginName}();
$item.setName(name);
${ clazz.attributeCode(item) }
$executor.create${clazz.pluginName}($item${if (parentClazz == null) "" else ", $parent"});
${references.addAllReferences}
${item.updateAsClass(clazz.pluginName)}
""".trimIndent()

private fun deleteScriptBody(className: String) = """
${item.retrieveExecutor}
${item.deleteAsClass(className)}
""".trimIndent()