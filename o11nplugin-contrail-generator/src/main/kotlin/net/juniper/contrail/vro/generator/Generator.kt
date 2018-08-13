/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.globalProjectInfo
import net.juniper.contrail.vro.config.isModelClass
import net.juniper.contrail.vro.config.isPluginClass
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.generator.model.generateSchemaInfo
import net.juniper.contrail.vro.generator.workflows.generateWorkflows
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.defaultSchema

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        generatePlugin()
    }

    @JvmStatic fun generatePlugin() {
        generatePlugin(globalProjectInfo, defaultSchema)
    }
}

fun generatePlugin(projectInfo: ProjectInfo, schema: Schema) {
    val objectClasses = objectClasses()
    val pluginClasses = objectClasses.filter { it.isPluginClass }
    val modelClasses = pluginClasses.filter { it.isModelClass }
    val propertyClasses = pluginClasses.propertyClasses()

    val relations = buildRelationDefinition(modelClasses)

    generateSchemaInfo(projectInfo, schema)
    generateModel(projectInfo, relations, pluginClasses, modelClasses, propertyClasses)
    generateWorkflows(projectInfo, relations, schema)
}