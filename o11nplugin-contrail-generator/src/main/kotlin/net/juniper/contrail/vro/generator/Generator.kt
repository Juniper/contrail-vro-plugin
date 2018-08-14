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
import net.juniper.contrail.vro.generator.workflows.generateWorkflows
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.defaultSchema

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        generatePlugin(globalProjectInfo, defaultSchema, defaultConfig)
    }
}

fun generatePlugin(projectInfo: ProjectInfo, schema: Schema, config: Config) {
    val objectClasses = objectClasses()
    val pluginClasses = objectClasses.filter { config.isPluginClass(it) }
    val modelClasses = pluginClasses.filter { config.isModelClass(it) }
    val propertyClasses = pluginClasses.propertyClasses()

    val relations = buildRelationDefinition(modelClasses, config)

    generateSchemaInfo(projectInfo, schema)
    generateModel(projectInfo, relations, pluginClasses, modelClasses, propertyClasses, config)
    generateWorkflows(projectInfo, schema, config, relations)
}