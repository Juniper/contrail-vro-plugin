/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ContrailUtilsKt
import net.juniper.contrail.vro.config.ProjectInfoKt
import net.juniper.contrail.vro.generator.model.RelationDefinitionKt
import net.juniper.contrail.vro.generator.workflows.WorkflowGeneratorKt
import net.juniper.contrail.vro.schema.SchemaKt
import spock.lang.Specification

abstract class GeneratorSpec extends Specification{
    private static def defaultTestConfig = TestConfigKt.defaultTestConfig

    def topLevelModel = Project
    def nonTopLevelModel = VirtualNetwork
    def parentChildPair = [Project, VirtualNetwork]
    def relatedPair = [VirtualNetwork, NetworkPolicy]
    def unrelatedPair = [NetworkIpam, SecurityGroup]

    def createConfig(HashMap settings) {
        return new Config(defaultTestConfig.copy(
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
                settings['validateSecurityScope'] ?: defaultTestConfig.validateSecurityScope,
                settings['draftClasses'] ?: defaultTestConfig.draftClasses
        ))
    }

    def generateSimpleWorkflows(Config config) {
        def relations = getRelations(config)
        WorkflowGeneratorKt.generateSimpleWorkflows(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, config).collect { it.displayName }
    }

    def getRelations(Config config) {
        def objectClasses = ContrailUtilsKt.objectClasses()
        def pluginClasses = objectClasses.findAll { config.isPluginClass(it) }
        def modelClasses = pluginClasses.findAll { config.isModelClass(it) }
        return RelationDefinitionKt.buildRelationDefinition(modelClasses, config)
    }

    def include(List<String> generatedWorkflows, List<String> expectedGeneratedWorkflows) {
        return generatedWorkflows.containsAll(expectedGeneratedWorkflows)
    }

    def notInclude(List<String> generatedWorkflows, List<String> notExpectedGeneratedWorkflows) {
        return generatedWorkflows.intersect(notExpectedGeneratedWorkflows).size() == 0
    }
}
