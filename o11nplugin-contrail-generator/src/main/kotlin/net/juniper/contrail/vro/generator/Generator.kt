/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.generator.model.buildRelationDefinition
import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.generator.util.objectClasses
import net.juniper.contrail.vro.generator.util.propertyClasses
import net.juniper.contrail.vro.generator.workflows.generateWorkflows

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val projectInfo = readProjectInfo()
        val objectClasses = objectClasses().filter { it.isModelClass }
        val rootClasses = objectClasses.filter { it.isRootClass }
        val propertyClasses = objectClasses.propertyClasses()

        val relations = buildRelationDefinition(objectClasses, rootClasses)
        generateModel(projectInfo, relations, objectClasses, rootClasses, propertyClasses)
        generateWorkflows(projectInfo, relations)
    }
}
