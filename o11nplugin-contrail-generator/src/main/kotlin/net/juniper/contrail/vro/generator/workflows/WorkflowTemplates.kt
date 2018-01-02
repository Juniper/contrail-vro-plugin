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
import net.juniper.contrail.vro.generator.workflows.model.boolean
import net.juniper.contrail.vro.generator.workflows.model.createDunesProperties
import net.juniper.contrail.vro.generator.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string
import net.juniper.contrail.vro.generator.workflows.model.workflow

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

    val nameInputDescription = "Connection name"
    val hostInputDescription = "Contrail host"
    val portInputDescription = "Contrail port"
    val usernameInputDescription = "User name"
    val passwordInputDescription = "User password"
    val authServerInputDescription = "Authentication server"
    val tenantInputDescription = "Tenant"

    return workflow(info, "Create Contrail connection") {
        input {
            parameter("name", string, nameInputDescription)
            parameter("host", string, hostInputDescription)
            parameter("port", number, portInputDescription)
            parameter("username", string, usernameInputDescription)
            parameter("password", SecureString, passwordInputDescription)
            parameter("authServer", string, authServerInputDescription)
            parameter("tenant", string, tenantInputDescription)
        }

        output {
            parameter("success", boolean)
        }

        items {
            script {
                body = createConnectionScriptBody

                inBinding("name", string)
                inBinding("host", string)
                inBinding("port", number)
                inBinding("username", string)
                inBinding("password", SecureString)
                inBinding("authServer", string)
                inBinding("tenant", string)

                outBinding("success", boolean)
            }
        }

        presentation {
            step("Controller") {
                parameter("name", nameInputDescription) {
                    mandatory = true
                    setDefaultValue("Controller")
                }
                parameter("host", hostInputDescription) {
                    mandatory = true
                }
                parameter("port", portInputDescription) {
                    mandatory = true
                    setDefaultValue(8082)
                    numberFormat = "#0"
                    minNumberValue = 0
                    maxNumberValue = 65535
                }
            }
            step("Credentials") {
                parameter("username", usernameInputDescription)
                parameter("password", passwordInputDescription)
                parameter("authServer", authServerInputDescription)
            }
            step("Tenant") {
                parameter("tenant", tenantInputDescription)
            }
        }
    }
}

fun createWorkflow(info: ProjectInfo, className: String, parentName: String): Workflow {

    val workflowName = "Create ${className.inWorkflowName}"
    val nameInputDescription = "${className.inDescription} name"
    val parentInputDescription = "Parent ${parentName.inDescription}"

    return workflow(info, workflowName) {
        input {
            parameter("name", string, nameInputDescription)
            parameter("parent", parentName.reference, parentInputDescription)
        }

        output {
            parameter("success", boolean)
        }

        items {
            script {
                body = createScriptBody(className, parentName)

                inBinding("name", string)
                inBinding("parent", parentName.reference)

                outBinding("success", boolean)
            }
        }

        presentation {
            step {
                parameter("parent", parentInputDescription) {
                    mandatory = true
                }
                parameter("name", nameInputDescription) {
                    mandatory = true
                }
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
    val finderName = clazz.asFinder
    val inputDescription = "${clazz.inDescription} to delete"

    return workflow(info, workflowName) {
        input {
            parameter("object", clazz.reference, inputDescription)
        }

        output {
            parameter("success", boolean)
        }

        items {
            script {
                body = scriptBody

                inBinding("object", clazz.reference)

                outBinding("success", boolean)
            }
        }

        presentation {
            step {
                parameter("object", inputDescription) {
                    mandatory = true
                    showInInventory = true
                }
            }
        }
    }
}

fun addReferenceWorkflow(info: ProjectInfo, relation: RefRelation): Workflow {

    val parentName = relation.parentName
    val childName = relation.childOriginalName
    val workflowName = "Add ${childName.inWorkflowName} to ${parentName.inWorkflowName}"
    val parentDescription = "${parentName.inDescription.capitalize()} to add to"
    val childDescription = "${childName.inDescription.capitalize()} to be added"
    val parentType = parentName.asFinder
    val childType = childName.asFinder

    return workflow(info, workflowName) {
        input {
            parameter("parent", parentName.reference, parentDescription)
            parameter("child", childName.reference, childDescription)
        }

        output {
            parameter("success", boolean)
        }

        items {
            script {
                body = addReferenceRelationScriptBody(relation)

                inBinding("parent", parentName.reference)
                inBinding("child", childName.reference)

                outBinding("success", boolean)
            }
        }

        presentation {
            step {
                parameter("parent", parentDescription) {
                    mandatory = true
                }
                parameter("child", childDescription) {
                    mandatory = true
                }
            }
        }
    }
}

fun removeReferenceWorkflow(info: ProjectInfo, relation: RefRelation): Workflow {

    val parentName = relation.parentName
    val childName = relation.childOriginalName
    val workflowName = "Remove ${childName.inWorkflowName} from ${parentName.inWorkflowName}"
    val parentDescription = "${parentName.inDescription.capitalize()} to remove from"
    val childDescription = "${childName.inDescription.capitalize()} to be removed"
    val parentType = parentName.asFinder
    val childType = childName.asFinder

    return workflow(info, workflowName) {
        input {
            parameter("parent", parentName.reference, parentDescription)
            parameter("child", childName.reference, childDescription)
        }

        output {
            parameter("success", boolean)
        }

        items {
            script {
                body = removeReferenceRelationScriptBody(relation)

                inBinding("parent", parentName.reference)
                inBinding("child", childName.reference)

                outBinding("success", boolean)
            }
        }

        presentation {
            step {
                parameter("parent", parentDescription) {
                    mandatory = true
                }
                parameter("child", childDescription) {
                    mandatory = true
                }
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

private fun addReferenceRelationScriptBody(relation: RefRelation) = """
${if (relation.simpleReference)
    "parent.add${relation.childOriginalName}(child);"
else {
    """var attribute = new Contrail${relation.referenceAttribute}();
    parent.add${relation.childOriginalName}(child, attribute);"""
}}
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update${relation.parentName}(parent);
""".trimIndent()

private fun removeReferenceRelationScriptBody(relation: RefRelation) = """
${if (relation.simpleReference)
    "parent.remove${relation.childOriginalName}(child);"
else {
    """var attribute = new Contrail${relation.referenceAttribute}();
    parent.remove${relation.childOriginalName}(child, null);"""
}}
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update${relation.parentName}(parent);
""".trimIndent()