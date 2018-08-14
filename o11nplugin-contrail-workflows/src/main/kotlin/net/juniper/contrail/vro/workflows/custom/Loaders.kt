/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.classesIn
import net.juniper.contrail.vro.config.isA
import net.juniper.contrail.vro.config.isStatic
import net.juniper.contrail.vro.config.parameterClass
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import java.lang.reflect.Method

private val workflowScriptsDirectory = "workflows"
private val actionScriptsDirectory = "actions"

object ScriptLoader {
    private fun load(path: String): String =
        ScriptLoader::class.java
            .getResourceAsStream(path)
            .bufferedReader()
            .use { it.readText() }

    fun loadActionScript(name: String): String =
        load("/$actionScriptsDirectory/$name.js")

    fun loadWorkflowScript(name: String): String =
        load("/$workflowScriptsDirectory/$name.js")
}

object WorkflowLoader {
    fun loadSimple(schema: Schema, config: Config): Sequence<WorkflowDefinition> =
        workflowClasses.flatMap { it.simpleWorkflowDefinitions(schema, config) }

    fun loadComplex(definitions: List<WorkflowDefinition>, schema: Schema, config: Config): Sequence<WorkflowDefinition> =
        workflowClasses.flatMap { it.complexWorkflowDefinitions(definitions, schema, config) }

    private val workflowClasses =
        classesIn(WorkflowLoader::class.java.`package`.name)
        .filter { it.hasWorkflowDefinitions }
}

object ActionLoader {
    fun load(): Sequence<ActionDefinition> =
        classesIn(ActionLoader::class.java.`package`.name)
        .filter { it.hasActionDefinitions }
        .flatMap { it.actionDefinitions() }
}

private val Class<*>.hasWorkflowDefinitions get() =
    declaredMethods.any { it.definesWorkflow }

private val Class<*>.hasActionDefinitions get() =
    declaredMethods.any { it.definesAction }

private fun Class<*>.simpleWorkflowDefinitions(schema: Schema, config: Config) =
    declaredMethods.asSequence()
        .filter { it.definesSimpleWorkflow }
        .map { it.createWorkflowDefinition(schema, config) }

private fun Class<*>.complexWorkflowDefinitions(definitions: List<WorkflowDefinition>, schema: Schema, config: Config) =
    declaredMethods.asSequence()
        .filter { it.definesComplexWorkflow }
        .map { it.createWorkflowDefinition(schema, config, definitions) }

private fun Class<*>.actionDefinitions() =
    declaredMethods.asSequence()
        .filter { it.definesAction }
        .map { it.createActionDefinition() }

private fun Method.createWorkflowDefinition(schema: Schema, config: Config, definitions: List<WorkflowDefinition> = listOf()) = when {
    takesNoParameters -> invoke(declaringClass)
    takesOnlySchema -> invoke(declaringClass, schema)
    takesSchemaAndConfig -> invoke(declaringClass, schema, config)
    takesOnlyWorkflowDefinitions -> invoke(declaringClass, definitions)
    takesWorkflowDefinitionsAndSchema -> invoke(declaringClass, definitions, schema)
    else -> throw IllegalArgumentException("Cannot create workflow definition using method '$this' of class '$declaringClass'.")
} as WorkflowDefinition

private fun Method.createActionDefinition(): ActionDefinition = when {
    takesNoParameters -> invoke(declaringClass)
    else -> throw IllegalArgumentException("Cannot create action definition using method '$this' of class '$declaringClass'.")
} as ActionDefinition

private val Method.definesWorkflow get() =
    isStatic && returnType == WorkflowDefinition::class.java

private val Method.definesSimpleWorkflow get() =
    definesWorkflow && takesSimpleWorkflowDefinitionParameters

private val Method.definesComplexWorkflow get() =
    definesWorkflow && takesComplexWorkflowDefinitionParameters

private val Method.takesSimpleWorkflowDefinitionParameters get() =
    takesNoParameters || takesOnlySchema || takesSchemaAndConfig

private val Method.takesComplexWorkflowDefinitionParameters get() =
    takesOnlyWorkflowDefinitions || takesWorkflowDefinitionsAndSchema

private val Method.takesNoParameters get() =
    parameterCount == 0

private val Method.takesOnlySchema get() =
    parameterCount == 1 && hasSchemaParameter

private val Method.takesSchemaAndConfig get() =
    parameterCount == 2 && hasSchemaParameter && hasConfigParameter

private val Method.takesOnlyWorkflowDefinitions get() =
    parameterCount == 1 && hasWorkflowDefinitionsParameter

private val Method.takesWorkflowDefinitionsAndSchema get() =
    parameterCount == 2 && hasWorkflowDefinitionsParameter && hasSchemaParameter

private val Method.hasSchemaParameter get() =
    parameters.any { it.type == Schema::class.java }

private val Method.hasConfigParameter get() =
    parameters.any { it.type == Config::class.java }

private val Method.hasWorkflowDefinitionsParameter get() =
    parameters.any { it.type.isA<List<*>>() && it.parameterizedType.parameterClass.isA<WorkflowDefinition>() }

private val Method.definesAction get() =
    isStatic && returnType == ActionDefinition::class.java && takesNoParameters

