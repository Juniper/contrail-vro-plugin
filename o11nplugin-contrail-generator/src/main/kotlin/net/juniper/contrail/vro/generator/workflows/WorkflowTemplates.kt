/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.model.RefRelation
import net.juniper.contrail.vro.generator.util.parentClassName
import net.juniper.contrail.vro.generator.util.splitCamel
import net.juniper.contrail.vro.generator.workflows.model.ElementType
import net.juniper.contrail.vro.generator.workflows.model.SecureString
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.model.addSingleScript
import net.juniper.contrail.vro.generator.workflows.model.createBasicWorkflow
import net.juniper.contrail.vro.generator.workflows.model.createDunesProperties
import net.juniper.contrail.vro.generator.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string

fun elementInfoPropertiesFor(workflow: Workflow, category: String) = createElementInfoProperties(
    categoryPath = "$libraryPackage.$category",
    type = ElementType.Workflow,
    name = workflow.displayName,
    id = workflow.id!!
)

fun dunesPropertiesFor(info: ProjectInfo) = createDunesProperties(
    pkgDescription = "Contrail package",
    pkgName = info.workflowsPackageName,
    usedPlugins = "Contrail#${info.baseVersion}",
    pkgOwner = "Juniper",
    pkgId = "4452345677834623546675023032605023032"
)

val Connection = "Connection"

private val String.inWorkflowName get() =
    splitCamel().toLowerCase()

private val String.inDescription get() =
    splitCamel()

private val String.asFinder get() =
    "Contrail:$this"

private val <T : ApiObjectBase> Class<T>.parentName get() =
    parentClassName ?: Connection

fun createConnectionWorkflow(info: ProjectInfo): Workflow {

    val workflowName = "Create Contrail connection"

    return createBasicWorkflow(info, workflowName).addSingleScript(createConnectionScriptBody) {
        step("Controller") {
            parameter("name", string) {
                description = "Connection name"
                mandatory = true
                defaultValue = "Controller"
            }
            parameter("host", string) {
                description = "Contrail host"
                mandatory = true
            }
            parameter("port", number) {
                description = "Contrail port"
                mandatory = true
                defaultValue = 8082
                min = 0
                max = 65535
            }
        }
        step("Credentials") {
            parameter("username", string) {
                description = "User name"
            }
            parameter("password", SecureString) {
                description = "User password"
            }
            parameter("authServer", string) {
                description = "Authentication server"
            }
        }
        step("Tenant") {
            parameter("tenant", string) {
                description = "Tenant"
            }
        }
    }
}

fun createWorkflow(info: ProjectInfo, className: String, parentName: String): Workflow {

    val workflowName = "Create ${className.inWorkflowName}"

    return createBasicWorkflow(info, workflowName).addSingleScript(createScriptBody(className, parentName)) {
        step {

            parameter("name", string) {
                description = "${className.inDescription} name"
                mandatory = true

            }
            parameter("parent", parentName.reference) {
                description = "Parent ${parentName.inDescription}"
                mandatory = true

            }
        }
    }
}

fun deleteConnectionWorkflow(info: ProjectInfo): Workflow =
    deleteWorkflow(info, Connection, deleteConnectionScriptBody)

fun deleteWorkflow(info: ProjectInfo, className: String): Workflow =
    deleteWorkflow(info, className, deleteScriptBody(className))

fun deleteWorkflow(info: ProjectInfo, clazz: String, scriptBody: String): Workflow {

    val workflowName = "Delete ${clazz.inWorkflowName}"

    return createBasicWorkflow(info, workflowName).addSingleScript(scriptBody) {
        step {
            parameter("object", clazz.reference) {
                description = "${clazz.inDescription} to delete"
                mandatory = true
                showInInventory = true
            }
        }
    }
}

fun addReferenceWorkflow(info: ProjectInfo, relation: RefRelation): Workflow {

    val parentName = relation.parentName
    val childName = relation.childOriginalName
    val workflowName = "Add ${childName.inWorkflowName} to ${parentName.inWorkflowName}"

    return createBasicWorkflow(info, workflowName).addSingleScript(relation.addReferenceRelationScriptBody()) {
        step {
            parameter("parent", parentName.reference) {
                description = "${parentName.inDescription.capitalize()} to add to"
                mandatory = true
            }
            parameter("child", childName.reference) {
                description = "${childName.inDescription.capitalize()} to be added"
                mandatory = true
            }
        }
    }
}

fun removeReferenceWorkflow(info: ProjectInfo, relation: RefRelation): Workflow {

    val parentName = relation.parentName
    val childName = relation.childOriginalName
    val workflowName = "Remove ${childName.inWorkflowName} from ${parentName.inWorkflowName}"

    return createBasicWorkflow(info, workflowName).addSingleScript(relation.removeReferenceRelationScriptBody()) {

        step {

            parameter("parent", parentName.reference) {
                description = "${parentName.inDescription.capitalize()} to remove from"
                mandatory = true
            }
            parameter("child", childName.reference) {
                description = "${childName.inDescription.capitalize()} to be removed"
                mandatory = true
            }
        }
    }
}

private val createConnectionScriptBody = """
var connectionId = ContrailConnectionManager.create(name, host, port, username, password, authServer, tenant);
System.log("Created connection with ID: " + connectionId);
""".trimIndent()

private val deleteConnectionScriptBody = """
ContrailConnectionManager.delete(object);
""".trimIndent()

private fun createScriptBody(className: String, parentName: String) = """
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
var element = new Contrail$className();
element.setName(name);
executor.create$className(element${if (parentName == Connection) "" else ", parent"});
""".trimIndent()

private fun deleteScriptBody(className: String) = """
var executor = ContrailConnectionManager.getExecutor(object.getInternalId().toString());
executor.delete$className(object);
""".trimIndent()

private fun RefRelation.addReferenceRelationScriptBody() = """
${if (simpleReference)
    "parent.add$childOriginalName(child);"
else {
    //TODO add to attribute properties to workflow parameters
    """var attribute = new Contrail$referenceAttributeSimpleName();
parent.add$childOriginalName(child, attribute);"""
}}
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update$parentName(parent);
""".trimIndent()

private fun RefRelation.removeReferenceRelationScriptBody() = """
${if (simpleReference)
    "parent.remove$childOriginalName(child);"
else
    "parent.remove$childOriginalName(child, null);"
}
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update$parentName(parent);
""".trimIndent()