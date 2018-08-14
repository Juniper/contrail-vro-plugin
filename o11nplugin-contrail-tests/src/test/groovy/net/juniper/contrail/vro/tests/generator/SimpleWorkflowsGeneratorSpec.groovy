package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.IdPermsType
import net.juniper.contrail.api.types.PermType2
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ContrailUtilsKt
import net.juniper.contrail.vro.config.ProjectInfoKt
import net.juniper.contrail.vro.generator.model.RelationDefinitionKt
import net.juniper.contrail.vro.generator.workflows.WorkflowGeneratorKt
import net.juniper.contrail.vro.schema.SchemaKt
import spock.lang.Specification

class SimpleWorkflowsGeneratorSpec extends Specification{

    def copyConfigContext(settings) {
        return TestConfigKt.defaultTestConfig.copy(
                settings['modelClasses'] ?: TestConfigKt.defaultTestConfig.modelClasses,
                settings['inventoryProperties'] ?: TestConfigKt.defaultTestConfig.inventoryProperties,
                settings['customPropertyObjects'] ?: TestConfigKt.defaultTestConfig.customPropertyObjects,
                settings['nonEssentialAttributes'] ?: TestConfigKt.defaultTestConfig.nonEssentialAttributes,
                settings['ignoredInWorkflows'] ?: TestConfigKt.defaultTestConfig.ignoredInWorkflows,
                settings['nonEditableProperties'] ?: TestConfigKt.defaultTestConfig.nonEditableProperties,
                settings['customPropertyValidation'] ?: TestConfigKt.defaultTestConfig.customPropertyValidation,
                settings['customCreateWorkflows'] ?: TestConfigKt.defaultTestConfig.customCreateWorkflows,
                settings['customEditWorkflows'] ?: TestConfigKt.defaultTestConfig.customEditWorkflows,
                settings['customDeleteWorkflows'] ?: TestConfigKt.defaultTestConfig.customDeleteWorkflows,
                settings['directChildren'] ?: TestConfigKt.defaultTestConfig.directChildren,
                settings['mandatoryReference'] ?: TestConfigKt.defaultTestConfig.mandatoryReference,
                settings['nonEditableReference'] ?: TestConfigKt.defaultTestConfig.nonEditableReference,
                settings['customAddReference'] ?: TestConfigKt.defaultTestConfig.customAddReference,
                settings['customRemoveReference'] ?: TestConfigKt.defaultTestConfig.customRemoveReference,
                settings['hiddenRoots'] ?: TestConfigKt.defaultTestConfig.hiddenRoots,
                settings['hiddenRelations'] ?: TestConfigKt.defaultTestConfig.hiddenRelations,
                settings['tagRelations'] ?: TestConfigKt.defaultTestConfig.tagRelations,
                settings['relationAsProperty'] ?: TestConfigKt.defaultTestConfig.relationAsProperty,
                settings['reversedRelations'] ?: TestConfigKt.defaultTestConfig.reversedRelations,
                settings['readUponQuery'] ?: TestConfigKt.defaultTestConfig.readUponQuery,
                settings['validateSecurityScope'] ?: TestConfigKt.defaultTestConfig.validateSecurityScope
        )
    }

    def "expected simple workflows are generated"() {
        given:
        def modelClassesSet = [VirtualNetwork.class.simpleName].toSet()
        def ignoredInWorkflows = [PermType2.class.simpleName, IdPermsType.class.simpleName].toSet()

        def configContext = copyConfigContext(modelClasses: modelClassesSet, ignoredInWorkflows: ignoredInWorkflows)

        def config = new Config(configContext)

        def objectClasses = ContrailUtilsKt.objectClasses()
        def pluginClasses = objectClasses.findAll { config.isPluginClass(it) }
        def modelClasses = pluginClasses.findAll { config.isModelClass(it) }
        def relations = RelationDefinitionKt.buildRelationDefinition(modelClasses, config)

        def workflowNames = ["Edit virtual network", "Delete virtual network"]

        when: "generating workflows"
        def generatedWorkflows = WorkflowGeneratorKt.generateSimpleWorkflows(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, config)

        then: "generated methods include all of the expected method names"
        generatedWorkflows.collect { it.displayName }.containsAll(workflowNames)
    }

    def "declared custom workflows are not generated"() {
        given:
        def modelClassesSet = [VirtualNetwork.class.simpleName].toSet()
        def ignoredInWorkflows = [PermType2.class.simpleName, IdPermsType.class.simpleName].toSet()
        def customEditWorkflows = [VirtualNetwork.class.simpleName].toSet()

        def configContext = copyConfigContext(
                modelClasses: modelClassesSet,
                ignoredInWorkflows: ignoredInWorkflows,
                customEditWorkflows: customEditWorkflows
        )

        def config = new Config(configContext)

        def objectClasses = ContrailUtilsKt.objectClasses()
        def pluginClasses = objectClasses.findAll { config.isPluginClass(it) }
        def modelClasses = pluginClasses.findAll { config.isModelClass(it) }
        def relations = RelationDefinitionKt.buildRelationDefinition(modelClasses, config)

        def generatedWorkflowNames = ["Delete virtual network"]
        def notGeneratedWorkflowNames = ["Edit virtual network"]

        when: "generating workflows"
        def generatedWorkflows = WorkflowGeneratorKt.generateSimpleWorkflows(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, config)

        then: "generated methods include all of the expected method names"
        generatedWorkflows.collect { it.displayName }.containsAll(generatedWorkflowNames)
        !generatedWorkflows.collect { it.displayName }.containsAll(notGeneratedWorkflowNames)
    }
}
