/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.classesIn
import net.juniper.contrail.vro.config.isStatic
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
    fun load(schema: Schema): Sequence<WorkflowDefinition> =
        classesIn(WorkflowLoader::class.java.`package`.name)
        .filter { it.hasWorkflowDefinitions }
        .flatMap { it.workflowDefinitions(schema) }
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

private fun Class<*>.workflowDefinitions(schema: Schema) =
    declaredMethods.asSequence()
        .filter { it.definesWorkflow }
        .map { it.createWorkflowDefinition(schema) }

private fun Class<*>.actionDefinitions() =
    declaredMethods.asSequence()
        .filter { it.definesAction }
        .map { it.createActionDefinition() }

private fun Method.createWorkflowDefinition(schema: Schema): WorkflowDefinition = when {
    takesNoParameters -> invoke(declaringClass)
    takesOnlySchema -> invoke(declaringClass, schema)
    else -> throw IllegalArgumentException("Cannot create workflow definition using method '$this' of class '$declaringClass'.")
} as WorkflowDefinition

private fun Method.createActionDefinition(): ActionDefinition = when {
    takesNoParameters -> invoke(declaringClass)
    else -> throw IllegalArgumentException("Cannot create action definition using method '$this' of class '$declaringClass'.")
} as ActionDefinition

private val Method.definesWorkflow get() =
    isStatic && returnType == WorkflowDefinition::class.java && takesWorkflowDefinitionParameters

private val Method.takesWorkflowDefinitionParameters get() =
    takesNoParameters || takesOnlySchema

private val Method.takesNoParameters get() =
    parameterCount == 0

private val Method.takesOnlySchema get() =
    parameterCount == 1 && parameters[0].type == Schema::class.java

private val Method.definesAction get() =
    isStatic && returnType == ActionDefinition::class.java && takesNoParameters

