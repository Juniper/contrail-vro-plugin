package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.vro.config.ContrailUtilsKt
import net.juniper.contrail.vro.config.ProjectInfoKt
import net.juniper.contrail.vro.generator.model.RelationDefinitionKt
import net.juniper.contrail.vro.generator.workflows.WorkflowGeneratorKt
import net.juniper.contrail.vro.schema.SchemaKt
import net.juniper.contrail.vro.tests.TestUtilsKt
import spock.lang.Specification

class SimpleWorkflowsGeneratorSpec extends Specification{
    def "expected simple workflows are generated"() {
        given:
        def config = TestConfigKt.getTestConfig()
        def objectClasses = ContrailUtilsKt.objectClasses()
        def pluginClasses = objectClasses.findAll { config.isPluginClass(it) }
        def modelClasses = pluginClasses.findAll { config.isModelClass(it) }
        def relations = RelationDefinitionKt.buildRelationDefinition(modelClasses, config)
        def workflowNames = TestUtilsKt.generateWorkflowNames(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, config)
        when: "generating workflows"
        def generatedWorkflows = WorkflowGeneratorKt.generateWorkflowDefinitions(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, TestConfigKt.getTestConfig())
        then: "generated methods include all of the expected method names"
        generatedWorkflows.collect { it.displayName }.containsAll(workflowNames)
    }
}
