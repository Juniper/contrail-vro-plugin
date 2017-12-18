/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.packageToPath
import net.juniper.contrail.vro.workflows.model.Workflow
import net.juniper.contrail.vro.workflows.model.Properties
import java.io.File
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

fun runWorkflowGenerator(info: ProjectInfo, objectClasses: List<Class<out ApiObjectBase>>) {
    createDunesMetaInfo(info)
    createConnectionWorkflow(info).saveInConfiguration(info)
    deleteConnectionWorkflow(info).saveInConfiguration(info)

    objectClasses.forEach {
        generateWorkflowsFor(it, info)
    }
}

private fun generateWorkflowsFor(clazz: Class<out ApiObjectBase>, info: ProjectInfo) {
    val category = clazz.simpleName

    createWorkflow(info, clazz).save(info, category)
    deleteWorkflow(info, clazz).save(info, category)
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
    val file = prepareDefinitionFile(info, category)

    workflowMarshaller.marshal(this, file)
}

private fun Workflow.generateElementInfo(info: ProjectInfo, category: String) {

    val file = prepareElementInfoFile(info, category)
    val properties = elementInfoPropertiesFor(this, category)

    propertiesMarshaller.marshal(properties, file)
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

private inline fun outputDirectory(info: ProjectInfo, category: String) =
    "${info.packageRoot}/$workflowResources/$libraryPath/$category"

private inline fun Workflow.outputFileBase(info: ProjectInfo, category: String) =
    outputDirectory(info, category) + "/" + displayName

private fun Workflow.definitionFileName(info: ProjectInfo, category: String) =
    outputFileBase(info, category) + ".xml"

private fun Workflow.elementInfoFileName(info: ProjectInfo, category: String) =
    outputFileBase(info, category) + ".element_info.xml"

private fun Workflow.prepareDefinitionFile(info: ProjectInfo, category: String) : File =
    definitionFileName(info, category).asPreparedFile()

private fun Workflow.prepareElementInfoFile(info: ProjectInfo, category: String) : File =
    elementInfoFileName(info, category).asPreparedFile()

private fun createDunesMetaInfo(info: ProjectInfo) {
    val outputFile = "${info.packageRoot}/$dunesInfoPath/$dunesFileName".asPreparedFile()
    val properties = dunesProp(
        pkgDescriptionArg = "Contrail package",
        pkgNameArg = info.workflowsPackageName,
        usedPluginsArg = "Contrail#${info.baseVersion}",
        pkgOwnerArg = "Juniper",
        pkgIdArg = "4452345677834623546675023032605023032"
    )

    propertiesMarshaller.marshal(properties, outputFile)
}