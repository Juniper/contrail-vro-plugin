/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.config.inventoryPropertyFilter
import net.juniper.contrail.vro.config.modelClassFilter
import net.juniper.contrail.vro.config.rootClassFilter
import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.config.objectClasses
import net.juniper.contrail.vro.config.propertyClasses
import net.juniper.contrail.vro.generator.workflows.generateWorkflows

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val projectInfo = readProjectInfo()
        val objectClasses = objectClasses().filter(modelClassFilter)
        val rootClasses = objectClasses.filter(rootClassFilter)
        val propertyClasses = objectClasses.propertyClasses()

        val relations = buildRelationDefinition(objectClasses, rootClasses, inventoryPropertyFilter)
        generateModel(projectInfo, relations, objectClasses, rootClasses, propertyClasses)
        generateWorkflows(projectInfo, relations)
    }
}
