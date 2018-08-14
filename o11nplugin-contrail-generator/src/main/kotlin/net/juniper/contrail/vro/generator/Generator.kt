/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.defaultConfig
import net.juniper.contrail.vro.config.globalProjectInfo
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
import net.juniper.contrail.vro.generator.model.generateSchemaInfo
import net.juniper.contrail.vro.generator.workflows.generateComplexWorkflowsAndCustomActions
import net.juniper.contrail.vro.generator.workflows.generateSimpleWorkflows
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.defaultSchema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val config = defaultConfig
        val simpleWorkflows = generateSimpleWorkflows(config)
        generateSimpleWorkflows(config)
        generateModelAndSchemaInfo(globalProjectInfo, defaultSchema, config)
        createComplexWorkflowsAndCustomActions(globalProjectInfo, defaultSchema, config, simpleWorkflows)
    }

    @JvmStatic fun generateSimpleWorkflows(config: Config) : List<WorkflowDefinition> {
        return generateSimpleWorkflows(globalProjectInfo, defaultSchema, config)
    }
}

fun generateModelAndSchemaInfo(projectInfo: ProjectInfo, schema: Schema, config: Config) {
    val objectClasses = objectClasses()
    val pluginClasses = objectClasses.filter { config.isPluginClass(it) }
    val modelClasses = pluginClasses.filter { config.isModelClass(it) }
    val propertyClasses = pluginClasses.propertyClasses()

    val relations = buildRelationDefinition(modelClasses, config)

    generateSchemaInfo(projectInfo, schema)
    generateModel(projectInfo, relations, pluginClasses, modelClasses, propertyClasses, config)
}

fun generateSimpleWorkflows(projectInfo: ProjectInfo, schema: Schema, config: Config) : List<WorkflowDefinition> {
    val objectClasses = objectClasses()
    val pluginClasses = objectClasses.filter { config.isPluginClass(it) }
    val modelClasses = pluginClasses.filter { config.isModelClass(it) }
    val relations = buildRelationDefinition(modelClasses, config)
    return generateSimpleWorkflows(projectInfo, relations, schema, config)
}

fun createComplexWorkflowsAndCustomActions(projectInfo: ProjectInfo, schema: Schema, config: Config, simpleWorkflows: List<WorkflowDefinition>) {
    generateComplexWorkflowsAndCustomActions(projectInfo, schema, config, simpleWorkflows)
}