/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.hasParent
import net.juniper.contrail.vro.generator.objectClasses
import net.juniper.contrail.vro.generator.parentClassName
import net.juniper.contrail.vro.generator.splitCamel
import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
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
    runWorkflowsGenerator("/home/mfigurski/IdeaProjects/contrail-vro-plugin/o11nplugin-contrail-vro-package", "1.0", "1")
}

fun runWorkflowsGenerator(packageRoot: String, projectVersion: String, buildNumber: String) {
    val gen = WorkflowGenerator(packageRoot, projectVersion, buildNumber)
    val objectClasses = objectClasses()
    objectClasses.forEach {
        gen.createWorkflow(it)
        gen.deleteWorkflow(it, packageRoot)
    }
}

private fun outputDirectory(packageRoot: String, clazz: Class<out ApiObjectBase>) =
    "$packageRoot/src/main/resources/Workflow/Library/Contrail/${clazz.simpleName}"

class WorkflowGenerator(private val packageRoot: String, projectVersion: String, buildNumber: String) {
    val API_VERSION = "6.0.0"
    val VERSION = "$projectVersion.$buildNumber"
    val knownTypes = arrayOf(String::class.java, Boolean::class.java, Int::class.java)
    val context = JAXBContext.newInstance(Workflow::class.java)
    val marshaller = context.createMarshaller()

    init {
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.setProperty(CharacterEscapeHandler::class.java.name, DefaultCharacterEscapeHandler())
    }

    fun createWorkflow(clazz: Class<out ApiObjectBase>) {
        val workflow = Workflow()
        workflow.rootName = "item1"
        workflow.objectName = "workflow:name=generic"
        workflow.id = "create${clazz.simpleName}".hashCode().toString()
        workflow.version = VERSION
        workflow.apiVersion = API_VERSION
        workflow.restartMode = "1"
        workflow.resumeFromFailedMode = "0"
        workflow.displayName = "Create ${clazz.simpleName.splitCamel().toLowerCase()}"
        workflow.position = Position(45.0f, 10.0f)

        val input = Input()
        workflow.input = input

        val nameParam = Parameter("name", "string")
        nameParam.description = "${clazz.simpleName.splitCamel()} name"
        input.parameters.add(nameParam)

        val parentType =
            if (clazz.hasParent)
                "Contrail:${clazz.parentClassName}"
            else
                "Contrail:Connection"

        val parentParam = Parameter("parent", parentType)
        parentParam.description = clazz.parentClassName.splitCamel()
        input.parameters.add(parentParam)

        val output = Output()
        workflow.output = output
        val successParameter = Parameter("success", "boolean")
        output.parameters.add(successParameter)

        workflow.workflowItems.add(createEndWorkflowItem())

        val scriptableItem = WorkflowItem("item1", "task")
        workflow.workflowItems.add(scriptableItem)
        scriptableItem.outName = "item0"
        scriptableItem.displayName = "Scriptable task"
        scriptableItem.script = createConstructorScript(clazz)
        val inBind = InBinding()
        scriptableItem.inBinding = inBind
        inBind.binds.add(Bind("name", "string", "name"))
        inBind.binds.add(Bind("parent", parentType, "parent"))

        val outBind = OutBinding()
        scriptableItem.outBinding = outBind
        outBind.binds.add(Bind("success", "boolean", "success"))

        scriptableItem.position = Position(145.0f , 20.0f)

        val presentation = Presentation()
        workflow.presentation = presentation
        val pStep = PresentationStep()
        presentation.presentationStep.add(pStep)
        val parentParameter = PresentationParameter("parent")
        pStep.presentationParameters.add(parentParameter)
        parentParameter.description = "Parent"
        parentParameter.parameterQualifiers.add(createMandatoryQualifier())

        val nameParameter = PresentationParameter("name")
        pStep.presentationParameters.add(nameParameter)
        nameParameter.description = "${clazz.simpleName.splitCamel()} name"
        nameParameter.parameterQualifiers.add(createMandatoryQualifier())

        val rootDir = outputDirectory(packageRoot, clazz)
        val workflowFile = File("$rootDir/Create ${clazz.simpleName.splitCamel().toLowerCase()}.xml")
        workflowFile.parentFile.mkdirs()
        this.marshaller.marshal(workflow, workflowFile)
        this.marshaller.marshal(workflow, System.out)
        generateElementInfFile("create${clazz.simpleName}".hashCode().toString(), clazz, rootDir, "create")

    }

    fun createEndWorkflowItem(): WorkflowItem {
        val workflowItem = WorkflowItem("item0", "end")
        workflowItem.endMode = "0"
        workflowItem.position = Position(325.0f, 10.0f)
        return workflowItem
    }

    fun createMandatoryQualifier() =
        wrapConstraints("required", true) as ParameterQualifier

    fun createConstructorScript(clazz: Class<out ApiObjectBase>): WorkflowScript {
        val className = clazz.simpleName
        val script = WorkflowScript()
        script.encoded = "false"
        script.value = """
            |var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
            |var element = new Contrail$className();
            |element.setName(name);
            |executor.create$className(element, parent);
            |""".trimMargin().trim()

        return script
    }

    fun generateElementInfFile(id: String, clazz: Class<out ApiObjectBase>, rootDir: String, operation: String) {
        val workflowName = "${operation.toLowerCase().capitalize()} ${clazz.simpleName.splitCamel().toLowerCase()}"
        val xmlFile = File("$rootDir/$workflowName.element_info.xml")

        val toSave =
            """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>Exported from [server]</comment>
<entry key="categoryPath">Library.Contrail.${clazz.simpleName}</entry>
<entry key="name">$workflowName</entry>
<entry key="type">Workflow</entry>
<entry key="id">$id</entry>
</properties>"""
        xmlFile.writeText(toSave)
    }

    fun deleteWorkflow(clazz: Class<out ApiObjectBase>, packageRoot: String) {
        val workflow = Workflow()
        workflow.rootName = "item1"
        workflow.objectName = "workflow:name=generic"
        workflow.id = "delete${clazz.simpleName}".hashCode().toString()
        workflow.version = VERSION
        workflow.apiVersion = API_VERSION
        workflow.restartMode = "1"
        workflow.resumeFromFailedMode = "0"
        workflow.displayName = "Delete ${clazz.simpleName.splitCamel().toLowerCase()}"
        workflow.position = Position(100.0f, 100.0f)

        val input = Input()
        workflow.input = input

        val nameParam = Parameter("object", "Contrail:${clazz.simpleName}")
        nameParam.description = "${clazz.simpleName.splitCamel()} to remove"
        input.parameters.add(nameParam)

        val output = Output()
        workflow.output = output
        val successParameter = Parameter("success", "boolean")
        output.parameters.add(successParameter)

        workflow.workflowItems.add(createEndWorkflowItem())

        val scriptableItem = WorkflowItem("item1", "task")
        workflow.workflowItems.add(scriptableItem)
        scriptableItem.outName = "item0"
        scriptableItem.displayName = "Scriptable task"
        scriptableItem.script = createRemovingScript(clazz)
        val inBind = InBinding()
        scriptableItem.inBinding = inBind
        inBind.binds.add(Bind("object", "Contrail:${clazz.simpleName}", "object"))

        val outBind = OutBinding()
        scriptableItem.outBinding = outBind
        outBind.binds.add(Bind("success", "boolean", "success"))

        scriptableItem.position = Position(200.0f, 45.0f)

        val presentation = Presentation()
        workflow.presentation = presentation
        val pStep = PresentationStep()
        presentation.presentationStep.add(pStep)

        val nameParameter = PresentationParameter("object")
        pStep.presentationParameters.add(nameParameter)
        nameParameter.description = "${clazz.simpleName.splitCamel()} to remove"
        nameParameter.parameterQualifiers.add(createMandatoryQualifier())

        val rootDir = outputDirectory(packageRoot, clazz)
        val workflowFile = File("$rootDir/Delete ${clazz.simpleName.splitCamel().toLowerCase()}.xml")
        workflowFile.parentFile.mkdirs()
        this.marshaller.marshal(workflow, workflowFile)
        this.marshaller.marshal(workflow, System.out)
        generateElementInfFile("delete${clazz.simpleName}".hashCode().toString(), clazz, rootDir, "delete")

    }

    private fun createRemovingScript(clazz: Class<out ApiObjectBase>): WorkflowScript? {
        val className = clazz.simpleName
        val script = WorkflowScript()
        script.encoded = "false"
        script.value = """
            |var executor = ContrailConnectionManager.getExecutor(object.getInternalId().toString());
            |executor.delete$className(object);
            |""".trimMargin().trim()

        return script
    }
}