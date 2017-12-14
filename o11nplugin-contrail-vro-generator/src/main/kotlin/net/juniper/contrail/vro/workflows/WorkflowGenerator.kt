/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.isRootClass
import net.juniper.contrail.vro.generator.objectClasses
import net.juniper.contrail.vro.generator.parentClassName
import net.juniper.contrail.vro.generator.readProjectInfo
import net.juniper.contrail.vro.generator.splitCamel
import net.juniper.contrail.vro.workflows.model.Workflow
import net.juniper.contrail.vro.workflows.model.WorkflowScript
import net.juniper.contrail.vro.workflows.model.info.Entry
import net.juniper.contrail.vro.workflows.model.info.Properties
import net.juniper.contrail.vro.workflows.model.workflow
import java.io.File
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class DefaultCharacterEscapeHandler : CharacterEscapeHandler {
    override fun escape(ac: CharArray, i: Int, j: Int, flag: Boolean, writer: Writer) {
        writer.write(ac, i, j)
    }
}

fun main(args: Array<String>) {
    runWorkflowsGenerator(readProjectInfo())
}

fun runWorkflowsGenerator(projectInfo: ProjectInfo) {
    val packageRoot = projectInfo.packageRoot
    val objectClasses = objectClasses()
    val workflowVersion = "${projectInfo.baseVersion}.${projectInfo.buildNumber}"
//    objectClasses.forEach {
//        val workflow = createWorkflow(it, workflowVersion)
//        saveWorkflow(workflow, packageRoot, it, "create")
//        deleteWorkflow(it, workflowVersion)
//    }

    val workflow = createWorkflow(VirtualNetwork::class.java, workflowVersion)
    workflow.save(packageRoot, VirtualNetwork::class.java)
    deleteWorkflow(VirtualNetwork::class.java, workflowVersion)
    workflow.generateElementInfoFile(VirtualNetwork::class.java, packageRoot)

}

val workflowContext = JAXBContext.newInstance(Workflow::class.java)
val workflowMarshaller = workflowContext.createMarshaller().apply {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(CharacterEscapeHandler::class.java.name, DefaultCharacterEscapeHandler())
}

val propertiesContext = JAXBContext.newInstance(Properties::class.java)
val propertiesMarshaller = propertiesContext.createMarshaller().apply {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(
        "com.sun.xml.internal.bind.xmlHeaders",
        "\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">")
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
                script = createConstructorScript(clazz)

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

fun createConstructorScript(clazz: Class<out ApiObjectBase>): WorkflowScript {
    val className = clazz.simpleName
    val value = """
        |var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
        |var element = new Contrail$className();
        |element.setName(name);
        |executor.create$className(element, parent);
        |""".trimMargin().trim()

    return WorkflowScript(encoded = false, value = value)
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
                script = createRemovingScript(clazz)

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

private fun createRemovingScript(clazz: Class<out ApiObjectBase>): WorkflowScript? {
    val className = clazz.simpleName
    val value = """
        |var executor = ContrailConnectionManager.getExecutor(object.getInternalId().toString());
        |executor.delete$className(object);
        |""".trimMargin().trim()

    return WorkflowScript(encoded = false, value = value)
}

fun Workflow.save(packageRoot: String, clazz: Class<out ApiObjectBase>) {
    val fileName = "$displayName.xml"
    val libraryPath = "src/main/resources/Workflow/Library/Contrail"
    val workflowFile = File("$packageRoot/$libraryPath/${clazz.simpleName}/$fileName.xml")

    workflowFile.parentFile.mkdirs()
    workflowFile.createNewFile()

    workflowMarshaller.marshal(this, workflowFile)
}

fun Workflow.generateElementInfoFile(clazz: Class<out ApiObjectBase>, packageRoot: String) {
    val libraryPath = "$packageRoot/src/main/resources/Workflow/Library/Contrail"
    val fileName = "$displayName.element_info.xml"
    val xmlFile = File("$libraryPath/${clazz.simpleName}/$fileName")

    xmlFile.parentFile.mkdirs()
    xmlFile.createNewFile()

    TODO("https://stackoverflow.com/questions/277996/jaxb-remove-standalone-yes-from-generated-xml")

    val properties = Properties().apply {
        addEntries(
            Entry("categoryPath", "Library.Contrail.Configuration.${clazz.simpleName}"),
            Entry("name", displayName),
            Entry("type", "Workflow"),
            Entry("id", id))
    }

    propertiesMarshaller.marshal(properties, xmlFile)
}
