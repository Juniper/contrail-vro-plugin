/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */
package net.juniper.contrail.vro.tests

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.allLowerCase
import net.juniper.contrail.vro.config.isDefaultRoot
import net.juniper.contrail.vro.config.isPolicyManagement
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.generator.model.RelationDefinition
import net.juniper.contrail.vro.generator.workflows.complexPropertiesInRange
import net.juniper.contrail.vro.generator.workflows.hasAnyEditableProperty
import net.juniper.contrail.vro.generator.workflows.hasCustomAddWorkflow
import net.juniper.contrail.vro.generator.workflows.hasCustomRemoveWorkflow
import net.juniper.contrail.vro.generator.workflows.isEditable
import net.juniper.contrail.vro.generator.workflows.mandatoryReferencesOf
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName

fun setField(obj: Any, fieldName: String, fieldValue: Any) {
    val f1 = obj.javaClass.getDeclaredField(fieldName)
    f1.isAccessible = true
    f1.set(obj, fieldValue)
}

fun generateWorkflowNames(info: ProjectInfo, relations: RelationDefinition, schema: Schema, config: Config): List<String> {
    val workflowNames: MutableList<String> = mutableListOf()

    relations.modelClasses.filter { ! config.isInternal(it) }.forEach {
        val refs = relations.mandatoryReferencesOf(it, config)
        val lifecycleWorkflowNames = generateLifecycleWorkflowNames(info, it, refs, schema, config)
        workflowNames.addAll(lifecycleWorkflowNames)
    }

    relations.forwardRelations.forEach {
        val referenceWorkflows = generateReferenceWorkflowNames(info, it)
        workflowNames.addAll(referenceWorkflows)
    }

    return workflowNames
}

private fun generateLifecycleWorkflowNames(info: ProjectInfo, clazz: ObjectClass, refs: List<ObjectClass>, schema: Schema, config: Config): List<String> {
    val workflowNames: MutableList<String> = mutableListOf()
    if (!config.hasCustomCreateWorkflow(clazz))
        createWorkflowNames(clazz, config).forEach { workflowNames.add(it) }
    if (!config.hasCustomEditWorkflow(clazz)) {
        editWorkflowName(clazz, schema, config)?.also { workflowNames.add(it) }
        editComplexPropertiesWorkflowNames(clazz, schema, config).also { workflowNames.addAll(it) }
    }
    if (!config.hasCustomDeleteWorkflow(clazz))
        deleteWorkflowName(clazz.pluginName).also { workflowNames.add(it) }
    return workflowNames
}

fun createWorkflowNames(clazz: ObjectClass, config: Config): List<String> {
    val parentsInModel = config.numberOfParentsInModel(clazz)
    val rootParents = config.hasRootParent(clazz) && !config.isHiddenRoot(clazz)

    return config.parents(clazz).filter { config.isModelClass(it) || it.isDefaultRoot }
        .map { createWorkflowName(clazz, it, parentsInModel, rootParents) }
        .toList()
}

fun createWorkflowName(clazz: ObjectClass, parentClazz: ObjectClass, parentsInModel: Int, hasRootParents: Boolean): String {
    val nonRootParents = parentsInModel > 0
    val addInParent = (hasRootParents || parentsInModel > 1) &&
        ! parentClazz.isPolicyManagement && ! parentClazz.isDefaultRoot
    val addGlobal = (parentClazz.isDefaultRoot && nonRootParents) || parentClazz.isPolicyManagement

    val workflowBaseName = "Create " + if (addGlobal) "global " else ""
    val workflowNameSuffix = if (addInParent) " in ${parentClazz.allLowerCase}" else ""
    return workflowBaseName + clazz.allLowerCase + workflowNameSuffix
}

fun editWorkflowName(clazz: ObjectClass, schema: Schema, config: Config): String? {
    if (! clazz.hasAnyEditableProperty(schema, config)) return null
    return "Edit ${clazz.allLowerCase}"
}

fun deleteWorkflowName(className: String) =
    "Delete ${className.allLowerCase}"

fun editComplexPropertiesWorkflowNames(clazz: ObjectClass, schema: Schema, config: Config) =
    clazz.complexPropertiesInRange(2..3, schema, config, false, 0)
        .map { it.complexEditWorkflowNames(schema, config, 0) }
        .flatten()
        .toList()

fun Property.complexEditWorkflowNames(schema: Schema, config: Config, level: Int) =
    clazz.complexPropertiesInRange(1..2, schema, config, false, level)
        .map { editComplexPropertyWorkflowName(this, it) }

fun editComplexPropertyWorkflowName(rootProperty: Property, thisProperty: Property): String {
    val rootClass = rootProperty.parent
    return "Edit ${thisProperty.clazz.allLowerCase} of ${rootClass.allLowerCase}"
}

fun generateReferenceWorkflowNames(info: ProjectInfo, relation: ForwardRelation): List<String> {
    val workflowNames: MutableList<String> = mutableListOf()
    if (relation.isEditable) {
        if (!relation.hasCustomAddWorkflow)
            addReferenceWorkflowName(relation).also { workflowNames.add(it) }
        if (!relation.hasCustomRemoveWorkflow)
            removeReferenceWorkflowName(relation).also { workflowNames.add(it) }
    }
    return workflowNames
}

fun addReferenceWorkflowName(relation: ForwardRelation): String {
    val parentClass = relation.declaredParentClass
    val childClass = relation.declaredChildClass
    return addRelationWorkflowName(parentClass, childClass)
}

fun removeReferenceWorkflowName(relation: ForwardRelation): String {
    val parentClass = relation.declaredParentClass
    val childClass = relation.declaredChildClass
    return removeRelationWorkflowName(parentClass, childClass)
}