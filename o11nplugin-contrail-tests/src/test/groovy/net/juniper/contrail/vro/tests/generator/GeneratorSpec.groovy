/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.vro.config.ContrailUtilsKt
import net.juniper.contrail.vro.generator.model.RelationDefinitionKt
import spock.lang.Specification

abstract class GeneratorSpec extends Specification{
    private static def defaultTestConfig = TestConfigKt.defaultTestConfig


    def createConfigContext(settings) {
        return defaultTestConfig.copy(
                settings['modelClasses'] ?: defaultTestConfig.modelClasses,
                settings['inventoryProperties'] ?: defaultTestConfig.inventoryProperties,
                settings['customPropertyObjects'] ?: defaultTestConfig.customPropertyObjects,
                settings['nonEssentialAttributes'] ?: defaultTestConfig.nonEssentialAttributes,
                settings['ignoredInWorkflows'] ?: defaultTestConfig.ignoredInWorkflows,
                settings['nonEditableProperties'] ?: defaultTestConfig.nonEditableProperties,
                settings['customPropertyValidation'] ?: defaultTestConfig.customPropertyValidation,
                settings['customCreateWorkflows'] ?: defaultTestConfig.customCreateWorkflows,
                settings['customEditWorkflows'] ?: defaultTestConfig.customEditWorkflows,
                settings['customDeleteWorkflows'] ?: defaultTestConfig.customDeleteWorkflows,
                settings['directChildren'] ?: defaultTestConfig.directChildren,
                settings['mandatoryReference'] ?: defaultTestConfig.mandatoryReference,
                settings['nonEditableReference'] ?: defaultTestConfig.nonEditableReference,
                settings['customAddReference'] ?: defaultTestConfig.customAddReference,
                settings['customRemoveReference'] ?: defaultTestConfig.customRemoveReference,
                settings['hiddenRoots'] ?: defaultTestConfig.hiddenRoots,
                settings['hiddenRelations'] ?: defaultTestConfig.hiddenRelations,
                settings['tagRelations'] ?: defaultTestConfig.tagRelations,
                settings['relationAsProperty'] ?: defaultTestConfig.relationAsProperty,
                settings['reversedRelations'] ?: defaultTestConfig.reversedRelations,
                settings['readUponQuery'] ?: defaultTestConfig.readUponQuery,
                settings['validateSecurityScope'] ?: defaultTestConfig.validateSecurityScope
        )
    }

    def getRelations(config) {
        def objectClasses = ContrailUtilsKt.objectClasses()
        def pluginClasses = objectClasses.findAll { config.isPluginClass(it) }
        def modelClasses = pluginClasses.findAll { config.isModelClass(it) }
        return RelationDefinitionKt.buildRelationDefinition(modelClasses, config)
    }

    def include(generatedWorkflows, expectedGeneratedWorkflows) {
        return generatedWorkflows.collect { it.displayName }.containsAll(expectedGeneratedWorkflows)
    }

    def notInclude(generatedWorkflows, notExpectedGeneratedWorkflows) {
        return generatedWorkflows.collect { it.displayName }.intersect(notExpectedGeneratedWorkflows).size() == 0
    }
}
