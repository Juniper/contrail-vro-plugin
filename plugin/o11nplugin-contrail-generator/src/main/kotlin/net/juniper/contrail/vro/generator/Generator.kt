/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.inventoryPropertyFilter
import net.juniper.contrail.vro.config.modelClassFilter
import net.juniper.contrail.vro.config.div
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
import net.juniper.contrail.vro.generator.workflows.generateWorkflows
import net.juniper.contrail.vro.generator.workflows.xsd.buildSchema
import java.nio.file.Paths

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val projectInfo = readProjectInfo()

        val generatorRoot = Paths.get(projectInfo.generatorRoot)
        val pluginRoot = generatorRoot.parent
        val repositoryRoot = pluginRoot.parent

        val objectClasses = objectClasses().filter(modelClassFilter)
        val propertyClasses = objectClasses.propertyClasses()

        val schemaPath = repositoryRoot / schemaFilePath
        val schema = buildSchema(schemaPath)
        val relations = buildRelationDefinition(objectClasses, inventoryPropertyFilter)

        generateModel(projectInfo, relations, objectClasses, propertyClasses)
        generateWorkflows(projectInfo, relations, schema)
    }
}
