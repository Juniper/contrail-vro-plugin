/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.hasCustomAddReferenceWorkflow
import net.juniper.contrail.vro.config.hasCustomCreateWorkflow
import net.juniper.contrail.vro.config.hasCustomDeleteWorkflow
import net.juniper.contrail.vro.config.hasCustomEditWorkflow
import net.juniper.contrail.vro.config.hasCustomRemoveReferenceWorkflow
import net.juniper.contrail.vro.config.hasMultipleParentsInModel
import net.juniper.contrail.vro.config.hasParents
import net.juniper.contrail.vro.config.isInternal
import net.juniper.contrail.vro.config.isRelationEditable
import net.juniper.contrail.vro.config.isRelationMandatory
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.generator.model.RelationDefinition
import net.juniper.contrail.vro.config.packageToPath
import net.juniper.contrail.vro.workflows.custom.loadCustomActions
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.workflows.custom.loadCustomWorkflows
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.Element
import net.juniper.contrail.vro.workflows.model.Workflow
import net.juniper.contrail.vro.workflows.model.Properties
import net.juniper.contrail.vro.schema.Schema
import java.io.File
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

fun generateWorkflows(info: ProjectInfo, relations: RelationDefinition, schema: Schema) {
    generateDunesMetaInfo(info)

    relations.modelClasses.filter { ! it.isInternal }.forEach {
        val refs = relations.mandatoryReferencesOf(it)
        generateLifecycleWorkflows(info, it, refs, schema)
    }

    relations.forwardRelations.forEach {
        generateReferenceWorkflows(info, it, schema)
    }

    createCustomWorkflows(info, schema)
    createCustomActions(info, schema)
}

fun RelationDefinition.mandatoryReferencesOf(clazz: ObjectClass) =
    forwardRelations.asSequence()
        .filter { it.parentClass == clazz }
        .filter { clazz.isRelationMandatory(it.childClass) }
        .map { it.childClass }
        .toList()

val ForwardRelation.isEditable get() =
    parentClass.isRelationEditable(childClass)

val ForwardRelation.hasCustomAddWorkflow get() =
    parentClass.hasCustomAddReferenceWorkflow(childClass)

val ForwardRelation.hasCustomRemoveWorkflow get() =
    parentClass.hasCustomRemoveReferenceWorkflow(childClass)

private fun generateLifecycleWorkflows(info: ProjectInfo, clazz: ObjectClass, refs: List<ObjectClass>, schema: Schema) {
    if (!clazz.hasCustomCreateWorkflow)
        createWorkflows(clazz, refs, schema).forEach { it.save(info, clazz) }
    if (!clazz.hasCustomEditWorkflow) {
        editWorkflow(clazz, schema)?.save(info, clazz)
        editComplexPropertiesWorkflows(clazz, schema).forEach { it.save(info, clazz) }
    }
    if (!clazz.hasCustomDeleteWorkflow)
        deleteWorkflow(clazz).save(info, clazz)
}

private fun generateReferenceWorkflows(info: ProjectInfo, relation: ForwardRelation, schema: Schema) {
    if (relation.isEditable) {
        if (!relation.hasCustomAddWorkflow)
            addReferenceWorkflow(relation, schema).save(info, relation.declaredParentClass)
        if (!relation.hasCustomRemoveWorkflow)
            removeReferenceWorkflow(relation).save(info, relation.declaredParentClass)
    }
}

private fun createCustomWorkflows(info: ProjectInfo, schema: Schema) {
    loadCustomWorkflows(schema).forEach { it.save(info) }
}

private fun createCustomActions(info: ProjectInfo, schema: Schema) {
    loadCustomActions(info.workflowVersion, info.workflowPackage).forEach { it.save(info) }
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

private val ProjectInfo.workflowVersion get() =
    "$baseVersion.$buildNumber"

private fun Marshaller.applyDefaultSetup(): Marshaller {
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(CharacterEscapeHandler::class.java.name, DefaultCharacterEscapeHandler())
    return this
}

private fun WorkflowDefinition.save(info: ProjectInfo, categoryClass: Class<*>) =
    save(info, categoryClass.pluginName)

private fun Workflow.save(info: ProjectInfo, category: String) {
    val categoryPackage = "$libraryPackage.$category"
    generateDefinition(info, categoryPackage)
    generateElementInfo(info, categoryPackage)
}

private fun WorkflowDefinition.save(info: ProjectInfo) =
    save(info, category ?: throw IllegalStateException("Category of workflow $displayName was not defined."))

private fun WorkflowDefinition.save(info: ProjectInfo, category: String) =
    createWorkflow(info).save(info, category)

private fun WorkflowDefinition.createWorkflow(info: ProjectInfo) =
    createWorkflow(info.workflowPackage, info.workflowVersion)

private fun Action.save(info: ProjectInfo) {
    generateDefinition(info, packageName)
    generateElementInfo(info, packageName)
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
    val properties = dunesPropertiesFor(info.workflowPackage, info.baseVersion)

    propertiesMarshaller.marshal(properties, outputFile)
}

val libraryPackage = "Library.Contrail"
val resourcesPath = "src/main/resources"
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
