/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.util

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.allLowerCase
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

inline fun <reified Clazz : ApiObjectBase> createRelationWorkflowName() =
    createRelationWorkflowName(Clazz::class.java)

fun createRelationWorkflowName(clazz: Class<*>) =
    "Create ${clazz.allLowerCase}"

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
    createWorkflowName(Clazz::class.java)

fun createWorkflowName(parentClazz: Class<*>, clazz: Class<*>): String =
    "Create ${clazz.allLowerCase} in ${parentClazz.allLowerCase}"

fun createWorkflowName(clazz: Class<*>): String =
    "Create ${clazz.allLowerCase}"