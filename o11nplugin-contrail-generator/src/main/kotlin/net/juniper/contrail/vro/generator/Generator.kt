/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.vro.generator.model.generateModel
import net.juniper.contrail.vro.generator.util.objectClasses
import net.juniper.contrail.vro.generator.workflows.generateWorkflows

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val projectInfo = readProjectInfo()
        val objectClasses = objectClasses()

        generateModel(projectInfo, objectClasses)
        generateWorkflows(projectInfo, objectClasses)
    }
}
