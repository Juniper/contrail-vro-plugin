/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.model.RelationsModel
import net.juniper.contrail.vro.generator.util.packageToPath
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.model.Properties
import java.io.File
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

fun generateWorkflows(info: ProjectInfo, relationsModel: RelationsModel) {
    generateDunesMetaInfo(info)
    createConnectionWorkflow(info).saveInConfiguration(info)
    deleteConnectionWorkflow(info).saveInConfiguration(info)

    relationsModel.rootClassNames.forEach {
        generateLifecycleWorkflows(info, it)
    }

    relationsModel.relations.forEach {
        generateLifecycleWorkflows(info, it.childName, it.parentName)
    }
}

private fun generateLifecycleWorkflows(info: ProjectInfo, className: String, parentName: String = Connection) {
    createWorkflow(info, className, parentName).save(info, className)
    deleteWorkflow(info, className).save(info, className)
}

val workflowContext = JAXBContext.newInstance(Workflow::class.java)
val workflowMarshaller = workflowContext.createMarshaller().apply {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(CharacterEscapeHandler::class.java.name, DefaultCharacterEscapeHandler())
}

val propertiesContext = JAXBContext.newInstance(Properties::class.java)
val propertiesMarshaller = propertiesContext.createMarshaller().apply {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(Marshaller.JAXB_FRAGMENT, true)
    setProperty("com.sun.xml.internal.bind.xmlHeaders", """
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">""".trimIndent())
}

private fun Workflow.saveInConfiguration(info: ProjectInfo) =
    save(info, "Configuration")

private fun Workflow.save(info: ProjectInfo, category: String) {
    generateDefinition(info, category)
    generateElementInfo(info, category)
}

private class DefaultCharacterEscapeHandler : CharacterEscapeHandler {
    override fun escape(ac: CharArray, i: Int, j: Int, flag: Boolean, writer: Writer) {
        writer.write(ac, i, j)
    }
}

private fun Workflow.generateDefinition(info: ProjectInfo, category: String) {
    val outputFile = prepareDefinitionFile(info, category)

    workflowMarshaller.marshal(this, outputFile)
}

private fun Workflow.generateElementInfo(info: ProjectInfo, category: String) {
    val outputFile = prepareElementInfoFile(info, category)
    val properties = elementInfoPropertiesFor(this, category)

    propertiesMarshaller.marshal(properties, outputFile)
}

private fun generateDunesMetaInfo(info: ProjectInfo) {
    val outputFile = dunesOutputPath(info).asPreparedFile()
    val properties = dunesPropertiesFor(info)

    propertiesMarshaller.marshal(properties, outputFile)
}

val libraryPackage = "Library.Contrail"
val libraryPath = libraryPackage.packageToPath()
val resourcesPath = "templates/main/resources"
val workflowResources = "$resourcesPath/Workflow"
val dunesInfoPath = "$resourcesPath/META-INF"
val dunesFileName = "dunes-meta-inf.xml"

private fun File.prepare()
{
    parentFile.mkdirs()
    createNewFile()
}

private fun String.asPreparedFile(): File =
    File(this).apply { prepare( ) }

private fun outputDirectory(info: ProjectInfo, category: String) =
    "${info.packageRoot}/$workflowResources/$libraryPath/$category"

private fun Workflow.outputFileBase(info: ProjectInfo, category: String) =
    outputDirectory(info, category) + "/" + displayName

private fun Workflow.definitionFileName(info: ProjectInfo, category: String) =
    outputFileBase(info, category) + ".xml"

private fun Workflow.elementInfoFileName(info: ProjectInfo, category: String) =
    outputFileBase(info, category) + ".element_info.xml"

private fun Workflow.prepareDefinitionFile(info: ProjectInfo, category: String) : File =
    definitionFileName(info, category).asPreparedFile()

private fun Workflow.prepareElementInfoFile(info: ProjectInfo, category: String) : File =
    elementInfoFileName(info, category).asPreparedFile()

private fun dunesOutputPath(info: ProjectInfo) =
    "${info.packageRoot}/$dunesInfoPath/$dunesFileName"
