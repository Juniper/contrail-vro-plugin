/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.workflows.dsl.ParameterDefinition
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.inCategory
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow

val workflowScriptsDirectory = "workflows"
val actionScriptsDirectory = "actions"

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

fun WorkflowDefinition.withScriptFile(name: String, setup: ParameterDefinition) =
    withScript(ScriptLoader.loadWorkflowScript(name), setup)

inline fun <reified T : Any> customWorkflow(name: String) =
    workflow(name).inCategory(T::class.java.pluginName)