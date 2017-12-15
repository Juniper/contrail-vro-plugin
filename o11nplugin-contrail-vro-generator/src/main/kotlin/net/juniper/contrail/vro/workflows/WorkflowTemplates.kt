/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.isRootClass
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


fun createWorkflow(clazz: Class<out ApiObjectBase>, workflowVersion: String): Workflow {

    val parentType =
        if (clazz.isRootClass)
            "Contrail:Connection"
        else
            "Contrail:${clazz.parentClassName}"
    val displayName = "Create ${clazz.simpleName.splitCamel().toLowerCase()}"
    val className = clazz.simpleName.splitCamel()
    val parentClassName = clazz.parentClassName?.splitCamel()

    return workflow(displayName) {
        version = workflowVersion

        input {
            parameter("name", "string", "$className name")
            parameter("parent", parentType, parentClassName)
        }

        output {
            parameter("success", "boolean")
        }

        items {

            includeEnd()

            script {
                script = clazz.createScript

                inBinding("name", "string")
                inBinding("parent", parentType)

                outBinding("success", "boolean")
            }
        }

        presentation {
            step {
                parameter("parent", "Parent") {
                    mandatory = true
                }
                parameter("name", "$className name") {
                    mandatory = true
                }
            }
        }
    }
}


fun deleteWorkflow(clazz: Class<out ApiObjectBase>, workflowVersion: String): Workflow {

    val displayName = "Delete ${clazz.simpleName.splitCamel().toLowerCase()}"
    val className = clazz.simpleName.splitCamel()

    return workflow(displayName) {
        version = workflowVersion

        input {
            parameter("object", "Contrail:${clazz.simpleName}", "$className to delete")
        }

        output {
            parameter("success", "boolean")
        }

        items {

            includeEnd()

            script {
                script = clazz.deleteScript

                inBinding("object", "Contrail:${clazz.simpleName}")

                outBinding("success", "boolean")
            }
        }

        presentation {
            step {
                parameter("object", "$className to delete") {
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

private fun createScriptBody(className: String) = """
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
var element = new Contrail$className();
element.setName(name);
executor.create$className(element, parent);
""".trim()

private fun deleteScriptBody(className: String) = """
var executor = ContrailConnectionManager.getExecutor(object.getInternalId().toString());
executor.delete$className(object);
""".trim()
