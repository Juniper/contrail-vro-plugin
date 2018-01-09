/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.generator.util.parentClassName
import net.juniper.contrail.vro.generator.util.splitCamel
import net.juniper.contrail.vro.generator.workflows.model.SecureString
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.dsl.andParameters
import net.juniper.contrail.vro.generator.workflows.dsl.packagedIn
import net.juniper.contrail.vro.generator.workflows.dsl.withScript
import net.juniper.contrail.vro.generator.workflows.dsl.withVersion
import net.juniper.contrail.vro.generator.workflows.model.Action
import net.juniper.contrail.vro.generator.workflows.model.Element
import net.juniper.contrail.vro.generator.workflows.model.createDunesProperties
import net.juniper.contrail.vro.generator.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string

fun Element.elementInfoPropertiesFor(categoryPath: String) = createElementInfoProperties(
    categoryPath = categoryPath,
    type = elementType,
    name = outputName,
    id = id
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

private val <T : ApiObjectBase> Class<T>.parentName get() =
    parentClassName ?: Connection

val ProjectInfo.workfloVersion get() =
    "$baseVersion.$buildNumber"

fun ProjectInfo.versionOf(name: String) =
    name packagedIn workflowsPackageName withVersion workfloVersion

fun createConnectionWorkflow(info: ProjectInfo): Workflow {

    val workflowName = "Create Contrail connection"

    return info.versionOf(workflowName) withScript createConnectionScriptBody andParameters {
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

    return info.versionOf(workflowName) withScript createScriptBody(className, parentName) andParameters {
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

fun deleteConnectionWorkflow(info: ProjectInfo): Workflow =
    deleteWorkflow(info, Connection, deleteConnectionScriptBody)

fun deleteWorkflow(info: ProjectInfo, className: String): Workflow =
    deleteWorkflow(info, className, deleteScriptBody(className))

fun deleteWorkflow(info: ProjectInfo, className: String, scriptBody: String): Workflow {

    val workflowName = "Delete ${className.inWorkflowName}"

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter("object", className.reference) {
            description = "${className.inDescription} to delete"
            mandatory = true
            showInInventory = true
        }
    }
}

fun addReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation): Workflow {

    val parentName = relation.parentName
    val childName = relation.childName
    val workflowName = "Add ${childName.inWorkflowName} to ${parentName.inWorkflowName}"
    val scriptBody = relation.addReferenceRelationScriptBody()

    return info.versionOf(workflowName) withScript scriptBody andParameters {
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

fun removeReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation, action: Action): Workflow {

    val parentName = relation.parentName
    val childName = relation.childName
    val workflowName = "Remove ${childName.inWorkflowName} from ${parentName.inWorkflowName}"
    val scriptBody = relation.removeReferenceRelationScriptBody()

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter("parent", parentName.reference) {
            description = "${parentName.inDescription.capitalize()} to remove from"
            mandatory = true
        }
        parameter("child", childName.reference) {
            description = "${childName.inDescription.capitalize()} to be removed"
            mandatory = true
            dependsOn("parent")
            listedBy(action)
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

private fun ForwardRelation.addReferenceRelationScriptBody() = """
${if (simpleReference)
    "parent.add$childName(child);"
else {
    //TODO add attribute properties to workflow parameters
    """var attribute = new Contrail$referenceAttributeSimpleName();
parent.add$childName(child, attribute);"""
}}
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update$parentName(parent);
""".trimIndent()

private fun ForwardRelation.removeReferenceRelationScriptBody() = """
${if (simpleReference)
    "parent.remove$childName(child);"
else
    "parent.remove$childName(child, null);"
}
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update$parentName(parent);
""".trimIndent()