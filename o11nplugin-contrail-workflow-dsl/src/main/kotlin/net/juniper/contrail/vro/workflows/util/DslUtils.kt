/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.util

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.PolicyManagement
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.allLowerCase
import net.juniper.contrail.vro.config.isDefaultRoot
import net.juniper.contrail.vro.config.isPolicyManagement
import net.juniper.contrail.vro.config.toTitle
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.predefinedAnswers
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.relationDescription
import net.juniper.contrail.vro.workflows.dsl.BasicBuilder
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.dsl.PrimitiveParameterBuilder

inline fun <reified Parent : Any>
    BasicParameterBuilder<*>.propertyDescription(
    schema: Schema,
    convertParameterNameToXsd: Boolean = true,
    title: String = parameterName.toTitle(),
    schemaName: String = parameterName) = """
$title
${schema.propertyDescription<Parent>(schemaName, convertParameterNameToXsd)}
""".trim()

inline fun <reified Parent : Any, reified Child : Any> BasicBuilder.relationDescription(schema: Schema) =
    schema.relationDescription<Parent, Child>()

// Only enumerations existing in schema are of type string
inline fun <reified Parent : Any> PrimitiveParameterBuilder<String>.extractPredefinedAnswers(
    schema: Schema,
    convertParameterNameToXsd: Boolean = true) {
    predefinedAnswers = schema.predefinedAnswers<Parent>(parameterName, mandatory, convertParameterNameToXsd)
}

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> addRelationWorkflowName() =
    addRelationWorkflowName(Parent::class.java, Child::class.java)

fun addRelationWorkflowName(parentClazz: Class<*>, clazz: Class<*>) =
    "Add ${clazz.allLowerCase} to ${parentClazz.allLowerCase}"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> removeRelationWorkflowName() =
    removeRelationWorkflowName(Parent::class.java, Child::class.java)

fun removeRelationWorkflowName(parentClazz: Class<*>, clazz: Class<*>) =
    "Remove ${clazz.allLowerCase} from ${parentClazz.allLowerCase}"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> Schema.parentDescriptionInCreateRelation() =
    parentDescriptionInCreateRelation(Parent::class.java, Child::class.java)

fun Schema.parentDescriptionInCreateRelation(parentClazz: Class<*>, clazz: Class<*>) =
    "${parentClazz.allCapitalized} to add ${clazz.allCapitalized} to"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> parentDescriptionInRemoveRelation() =
    parentDescriptionInRemoveRelation(Parent::class.java, Child::class.java)

fun parentDescriptionInRemoveRelation(parentClazz: Class<*>, clazz: Class<*>) =
    "${parentClazz.allCapitalized} to remove ${clazz.allCapitalized} from"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> Schema.childDescriptionInCreateRelation(ignoreMissing: Boolean = false) =
    childDescriptionInCreateRelation(Parent::class.java, Child::class.java, ignoreMissing)

fun Schema.childDescriptionInCreateRelation(parentClazz: Class<*>, clazz: Class<*>, ignoreMissing: Boolean = false) = """
${clazz.allCapitalized} to be added
${relationDescription(parentClazz, clazz, ignoreMissing) ?: ""}
""".trim()

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> childDescriptionInRemoveRelation() =
    childDescriptionInRemoveRelation(Parent::class.java, Child::class.java)

fun childDescriptionInRemoveRelation(parentClazz: Class<*>, clazz: Class<*>) =
    "${clazz.allCapitalized} to be removed"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> createWorkflowName() =
    createWorkflowName(Parent::class.java, Child::class.java)

inline fun <reified Clazz : ApiObjectBase> createSimpleWorkflowName() =
    createSimpleWorkflowName(Clazz::class.java)

inline fun <reified Clazz : ApiObjectBase> createGlobalWorkflowName() =
    createGlobalWorkflowName(Clazz::class.java)

fun createWorkflowName(parentClazz: ObjectClass, clazz: ObjectClass): String =
    createWorkflowName(clazz, parentClazz, 0, true)

fun createSimpleWorkflowName(clazz: ObjectClass): String =
    createWorkflowName(clazz, clazz, 0, false)

fun createGlobalWorkflowName(clazz: ObjectClass): String =
    createWorkflowName(clazz, PolicyManagement::class.java, 0, false)

fun createWorkflowName(clazz: ObjectClass, parentClazz: ObjectClass, parentsInModel: Int, hasRootParents: Boolean): String {
    val nonRootParents = parentsInModel > 0
    val addInParent = (hasRootParents || parentsInModel > 1) &&
        !parentClazz.isPolicyManagement && ! parentClazz.isDefaultRoot
    val addGlobal = (parentClazz.isDefaultRoot && nonRootParents) || parentClazz.isPolicyManagement

    val workflowBaseName = "Create " + if (addGlobal) "global " else ""
    val workflowNameSuffix = if (addInParent) " in ${parentClazz.allLowerCase}" else ""
    return workflowBaseName + clazz.allLowerCase + workflowNameSuffix
}

fun deleteWorkflowName(clazz: ObjectClass): String =
    deleteWorkflowName(clazz.simpleName)

fun deleteWorkflowName(className: String): String =
    "Delete ${className.allLowerCase}"

fun editWorkflowName(clazz: ObjectClass): String =
    "Edit ${clazz.allLowerCase}"