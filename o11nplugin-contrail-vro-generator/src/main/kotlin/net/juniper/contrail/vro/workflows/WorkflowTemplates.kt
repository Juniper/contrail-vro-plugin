/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.parentClassName
import net.juniper.contrail.vro.generator.splitCamel
import net.juniper.contrail.vro.workflows.model.Properties
import net.juniper.contrail.vro.workflows.model.Workflow
import net.juniper.contrail.vro.workflows.model.properties
import net.juniper.contrail.vro.workflows.model.workflow

fun propertiesFor(workflow: Workflow, category: String) : Properties {
    return properties {
        categoryPath = "$libraryPackage.$category"
        type = Workflow
        name = workflow.displayName
        id = workflow.id
    }
}

private val Connection = "Connection"

private val String.inWorkflowName get() =
    splitCamel().toLowerCase()

private val String.inDescription get() =
    splitCamel()

private val String.asFinder get() =
    "Contrail:$this"

private val <T : ApiObjectBase> Class<T>.parentName get() =
    parentClassName ?: Connection

fun createConnectionWorkflow(): Workflow {

    val nameInputDescription = "Connection name"

    return workflow("Create Contrail connection") {
        input {
            parameter("name", "string", nameInputDescription)
        }

        output {
            parameter("success", "boolean")
        }

        items {
            script {
                body = createConnectionScriptBody

                inBinding("name", "string")

                outBinding("success", "boolean")
            }
        }

        presentation {
            step {
                parameter("name", nameInputDescription) {
                    mandatory = true
                }
            }
        }
    }
}

fun createWorkflow(clazz: Class<out ApiObjectBase>): Workflow {

    val workflowName = "Create ${clazz.simpleName.inWorkflowName}"
    val nameInputDescription = "${clazz.simpleName.inDescription} name"
    val parentType = clazz.parentName.asFinder
    val parentInputDescription = "Parent ${clazz.parentName.inDescription}"

    return workflow(workflowName) {
        input {
            parameter("name", "string", nameInputDescription)
            parameter("parent", parentType, parentInputDescription)
        }

        output {
            parameter("success", "boolean")
        }

        items {
            script {
                body = clazz.createScript

                inBinding("name", "string")
                inBinding("parent", parentType)

                outBinding("success", "boolean")
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

fun deleteConnectionWorkflow(): Workflow =
    deleteWorkflow("Connection", deleteConnectionScriptBody)

fun deleteWorkflow(clazz: Class<out ApiObjectBase>): Workflow =
    deleteWorkflow(clazz.simpleName, clazz.deleteScript)

fun deleteWorkflow(clazz: String, scriptBody: String): Workflow {

    val workflowName = "Delete ${clazz.inWorkflowName}"
    val finderName = clazz.asFinder
    val inputDescription = "${clazz.inDescription} to delete"

    return workflow(workflowName) {
        input {
            parameter("object", finderName, inputDescription)
        }

        output {
            parameter("success", "boolean")
        }

        items {
            script {
                body = scriptBody

                inBinding("object", finderName)

                outBinding("success", "boolean")
            }
        }

        presentation {
            step {
                parameter("object", inputDescription) {
                    mandatory = true
                }
            }
        }
    }
}

private val <T : ApiObjectBase> Class<T>.createScript get() =
    createScriptBody(simpleName)

private val <T : ApiObjectBase> Class<T>.deleteScript get() =
    deleteScriptBody(simpleName)

private val createConnectionScriptBody = """
var connectionId = ContrailConnectionManager.create(name, host, port, username, password, authServer, tenant);
System.log("Created connection with ID: " + connectionId);
""".trimIndent()

private val deleteConnectionScriptBody = """
ContrailConnectionManager.delete(connection);
""".trimIndent()

private fun createScriptBody(className: String) = """
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
var element = new Contrail$className();
element.setName(name);
executor.create$className(element, parent);
""".trimIndent()

private fun deleteScriptBody(className: String) = """
var executor = ContrailConnectionManager.getExecutor(object.getInternalId().toString());
executor.delete$className(object);
""".trimIndent()
