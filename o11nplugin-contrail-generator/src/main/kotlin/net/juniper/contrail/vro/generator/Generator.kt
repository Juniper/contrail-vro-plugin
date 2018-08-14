/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.DefaultConfig
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.globalProjectInfo
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
import net.juniper.contrail.vro.generator.workflows.generateWorkflowDefinitions
import net.juniper.contrail.vro.generator.model.generateSchemaInfo
import net.juniper.contrail.vro.generator.workflows.generateWorkflows
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.defaultSchema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val config = Config.getInstance(DefaultConfig)
        val workflows = getWorkflows(config)
        generateModel(config)
        createWorkflows(config, workflows)
    }

    @JvmStatic fun getWorkflows(config: Config) : List<WorkflowDefinition> {
        return getWorkflows(globalProjectInfo, defaultSchema, config)
    }

    @JvmStatic fun generateModel(config: Config) {
        generateModel(globalProjectInfo, defaultSchema, config)
    }

    @JvmStatic fun createWorkflows(config: Config, workflows: List<WorkflowDefinition>) {
        createAllWorkflows(globalProjectInfo, defaultSchema, config, workflows)
    }
}

fun generateModel(projectInfo: ProjectInfo, schema: Schema, config: Config) {
    val objectClasses = objectClasses()
    val pluginClasses = objectClasses.filter { config.isPluginClass(it) }
    val modelClasses = pluginClasses.filter { config.isModelClass(it) }
    val propertyClasses = pluginClasses.propertyClasses()

    val relations = buildRelationDefinition(modelClasses, config)

    generateSchemaInfo(projectInfo, schema)
    generateModel(projectInfo, relations, pluginClasses, modelClasses, propertyClasses, config)
}

fun getWorkflows(projectInfo: ProjectInfo, schema: Schema, config: Config) : List<WorkflowDefinition> {
    val objectClasses = objectClasses()
    val pluginClasses = objectClasses.filter { config.isPluginClass(it) }
    val modelClasses = pluginClasses.filter { config.isModelClass(it) }
    val relations = buildRelationDefinition(modelClasses, config)
    return generateWorkflowDefinitions(projectInfo, relations, schema, config)
}

fun createAllWorkflows(projectInfo: ProjectInfo, schema: Schema, config: Config, workflows: List<WorkflowDefinition>) {
    generateWorkflows(projectInfo, schema, config, workflows)
}