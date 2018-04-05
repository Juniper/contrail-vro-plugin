/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.util

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.allLowerCase
import net.juniper.contrail.vro.config.toTitle
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.dsl.BasicBuilder
import net.juniper.contrail.vro.workflows.dsl.PrimitiveParameterBuilder
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.relationDescription
import net.juniper.contrail.vro.workflows.schema.predefinedAnswers

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

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> Schema.addRelationWorkflowName() =
    addRelationWorkflowName(Parent::class.java, Child::class.java)

fun Schema.addRelationWorkflowName(parentClazz: Class<*>, clazz: Class<*>) =
    "Add ${clazz.allLowerCase} to ${parentClazz.allLowerCase}"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> removeRelationWorkflowName() =
    removeRelationWorkflowName(Parent::class.java, Child::class.java)

fun removeRelationWorkflowName(parentClazz: Class<*>, clazz: Class<*>) =
    "Remove ${clazz.allLowerCase} from ${parentClazz.allLowerCase}"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> Schema.parentDescriptionInCreateRelation() =
    parentDescriptionInCreateRelation(Parent::class.java, Child::class.java)

fun Schema.parentDescriptionInCreateRelation(parentClazz: Class<*>, clazz: Class<*>) =
    "${parentClazz.allCapitalized} to add ${clazz.allCapitalized} to"

fun parentDescriptionInRemoveRelation(parentClazz: Class<*>, clazz: Class<*>) =
    "${parentClazz.allCapitalized} to remove ${clazz.allCapitalized} from"

inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase> Schema.childDescriptionInCreateRelation() =
    childDescriptionInCreateRelation(Parent::class.java, Child::class.java)

fun Schema.childDescriptionInCreateRelation(parentClazz: Class<*>, clazz: Class<*>, ignoreMissing: Boolean = false) = """
${clazz.allCapitalized} to be added
${relationDescription(parentClazz, clazz, ignoreMissing) ?: ""}
""".trim()

fun childDescriptionInRemoveRelation(parentClazz: Class<*>, clazz: Class<*>) =
    "${clazz.allCapitalized} to be removed"