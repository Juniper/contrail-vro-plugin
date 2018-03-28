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

fun WorkflowDefinition.withScriptFile(name: String, setup: ParameterDefinition) =
    withScript(ScriptLoader.loadWorkflowScript(name), setup)

inline fun <reified T : Any> customWorkflow(name: String) =
    workflow(name).inCategory(T::class.java.pluginName)

fun String.toScriptName() =
    split(' ').joinToString("") { it.capitalize() }.decapitalize()