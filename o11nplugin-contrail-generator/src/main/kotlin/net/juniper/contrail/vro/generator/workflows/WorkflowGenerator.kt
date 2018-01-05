/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.model.RefRelation
import net.juniper.contrail.vro.generator.model.RelationDefinition
import net.juniper.contrail.vro.generator.util.packageToPath
import net.juniper.contrail.vro.generator.workflows.model.Action
import net.juniper.contrail.vro.generator.workflows.model.Element
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.model.Properties
import java.io.File
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

fun generateWorkflows(info: ProjectInfo, relations: RelationDefinition) {
    generateDunesMetaInfo(info)
    createConnectionWorkflow(info).saveInConfiguration(info)
    deleteConnectionWorkflow(info).saveInConfiguration(info)

    relations.rootClasses.forEach {
        generateLifecycleWorkflows(info, it.simpleName)
    }

    relations.relations.forEach {
        generateLifecycleWorkflows(info, it.childName, it.parentName)
    }

    relations.referenceRelations.asSequence()
        .filter { !it.backReference }
        .forEach {
            generateReferenceWorkflows(info, it)
    }
}

private fun generateLifecycleWorkflows(info: ProjectInfo, className: String, parentName: String = Connection) {
    createWorkflow(info, className, parentName).save(info, className)
    deleteWorkflow(info, className).save(info, className)
}

private fun generateReferenceWorkflows(info: ProjectInfo, relation: RefRelation) {
    addReferenceWorkflow(info, relation).save(info, relation.parentName)
    removeReferenceWorkflow(info, relation).save(info, relation.parentName)
}

val workflowContext = JAXBContext.newInstance(Workflow::class.java)
val workflowMarshaller = workflowContext.createMarshaller().applyDefaultSetup()

val actionContext = JAXBContext.newInstance(Action::class.java)
val actionMarshaller = actionContext.createMarshaller().applyDefaultSetup()

val propertiesContext = JAXBContext.newInstance(Properties::class.java)
val propertiesMarshaller = propertiesContext.createMarshaller().apply {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(Marshaller.JAXB_FRAGMENT, true)
    setProperty("com.sun.xml.internal.bind.xmlHeaders", """
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">""".trimIndent())
}

private fun Marshaller.applyDefaultSetup(): Marshaller {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(CharacterEscapeHandler::class.java.name, DefaultCharacterEscapeHandler())
    return this
}

private fun Workflow.saveInConfiguration(info: ProjectInfo) =
    save(info, "Configuration")

private fun Workflow.save(info: ProjectInfo, category: String) {
    val categoryPackage = "$libraryPackage.$category"
    generateDefinition(info, categoryPackage)
    generateElementInfo(info, categoryPackage)
}

private fun Action.save(info: ProjectInfo) {
    val categoryPackage = info.workflowsPackageName
    generateDefinition(info, categoryPackage)
    generateElementInfo(info, categoryPackage)
}

private class DefaultCharacterEscapeHandler : CharacterEscapeHandler {
    override fun escape(ac: CharArray, i: Int, j: Int, flag: Boolean, writer: Writer) {
        writer.write(ac, i, j)
    }
}

private fun Element.generateDefinition(marshaller: Marshaller, packageRoot: String, categoryPackage: String) {
    val outputFile = prepareDefinitionFile(packageRoot, categoryPackage)

    marshaller.marshal(this, outputFile)
}

private fun Workflow.generateDefinition(info: ProjectInfo, categoryPackage: String) =
    generateDefinition(workflowMarshaller, info.packageRoot, categoryPackage)

private fun Action.generateDefinition(info: ProjectInfo, categoryPackage: String) =
    generateDefinition(actionMarshaller, info.packageRoot, categoryPackage)

private fun Element.generateElementInfo(info: ProjectInfo, categoryPath: String) {
    val outputFile = prepareElementInfoFile(info.packageRoot, categoryPath)
    val properties = elementInfoPropertiesFor(categoryPath)

    propertiesMarshaller.marshal(properties, outputFile)
}

private fun generateDunesMetaInfo(info: ProjectInfo) {
    val outputFile = dunesOutputPath(info).asPreparedFile()
    val properties = dunesPropertiesFor(info)

    propertiesMarshaller.marshal(properties, outputFile)
}

val libraryPackage = "Library.Contrail"
val resourcesPath = "templates/main/resources"
val dunesInfoPath = "$resourcesPath/META-INF"
val dunesFileName = "dunes-meta-inf.xml"

private fun File.prepare()
{
    parentFile.mkdirs()
    createNewFile()
}

private fun String.asPreparedFile(): File =
    File(this).apply { prepare( ) }

private fun Element.outputDirectory(packageRoot: String, categoryPackage: String) =
    "$packageRoot/$resourcesPath/$elementType/${categoryPackage.packageToPath()}/"

private fun Element.outputFileBase(packageRoot: String, categoryPackage: String) =
    outputDirectory(packageRoot, categoryPackage) + outputName

private fun Element.definitionFileName(packageRoot: String, categoryPackage: String) =
    outputFileBase(packageRoot, categoryPackage) + ".xml"

private fun Element.elementInfoFileName(packageRoot: String, categoryPackage: String) =
    outputFileBase(packageRoot, categoryPackage) + ".element_info.xml"

private fun Element.prepareDefinitionFile(packageRoot: String, categoryPackage: String) : File =
    definitionFileName(packageRoot, categoryPackage).asPreparedFile()

private fun Element.prepareElementInfoFile(packageRoot: String, categoryPackage: String) : File =
    elementInfoFileName(packageRoot, categoryPackage).asPreparedFile()

private fun dunesOutputPath(info: ProjectInfo) =
    "${info.packageRoot}/$dunesInfoPath/$dunesFileName"
