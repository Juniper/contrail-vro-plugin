/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.inventoryPropertyFilter
import net.juniper.contrail.vro.config.modelClassFilter
import net.juniper.contrail.vro.config.globalProjectInfo
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
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
    val objectClasses = objectClasses().filter(modelClassFilter)
    val propertyClasses = objectClasses.propertyClasses()

    val relations = buildRelationDefinition(objectClasses, inventoryPropertyFilter)

    generateModel(projectInfo, relations, objectClasses, propertyClasses)
    generateWorkflows(projectInfo, relations, schema)
}
