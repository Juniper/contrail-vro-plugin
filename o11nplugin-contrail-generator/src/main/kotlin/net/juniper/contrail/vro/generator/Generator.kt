/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.inventoryPropertyFilter
import net.juniper.contrail.vro.config.modelClassFilter
import net.juniper.contrail.vro.config.div
import net.juniper.contrail.vro.config.globalProjectInfo
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
import net.juniper.contrail.vro.generator.workflows.generateWorkflows
import net.juniper.contrail.vro.workflows.schema.buildSchema
import java.nio.file.Paths

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        generatePlugin(globalProjectInfo)
    }
}

fun generatePlugin(projectInfo: ProjectInfo) {
    val objectClasses = objectClasses().filter(modelClassFilter)
    val propertyClasses = objectClasses.propertyClasses()

    val schema = buildSchema(Paths.get(projectInfo.schemaFile))
    val relations = buildRelationDefinition(objectClasses, inventoryPropertyFilter)

    generateModel(projectInfo, relations, objectClasses, propertyClasses)
    generateWorkflows(projectInfo, relations, schema)
}
