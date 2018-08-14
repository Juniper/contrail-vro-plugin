/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.IdPermsType
import net.juniper.contrail.api.types.PermType2
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ProjectInfoKt
import net.juniper.contrail.vro.generator.workflows.WorkflowGeneratorKt
import net.juniper.contrail.vro.schema.SchemaKt
import net.juniper.contrail.vro.workflows.util.DslUtilsKt

class SimpleWorkflowsGeneratorSpec extends GeneratorSpec{

    def "expected simple workflows are generated"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName].toSet()
        def ignoredInWorkflows = [PermType2.class.simpleName, IdPermsType.class.simpleName].toSet()

        def configContext = createConfigContext(modelClasses: modelClasses, ignoredInWorkflows: ignoredInWorkflows)

        def config = new Config(configContext)
        def relations = getRelations(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork), DslUtilsKt.deleteWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflows = WorkflowGeneratorKt.generateSimpleWorkflows(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflows, expectedGeneratedWorkflows)
    }

    def "declared custom workflows are not generated"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName].toSet()
        def ignoredInWorkflows = [PermType2.class.simpleName, IdPermsType.class.simpleName].toSet()
        def customEditWorkflows = [VirtualNetwork.class.simpleName].toSet()

        def configContext = createConfigContext(
            modelClasses: modelClasses,
            ignoredInWorkflows: ignoredInWorkflows,
            customEditWorkflows: customEditWorkflows
        )

        def config = new Config(configContext)
        def relations = getRelations(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(VirtualNetwork)]
        def notExpectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflows = WorkflowGeneratorKt.generateSimpleWorkflows(ProjectInfoKt.globalProjectInfo, relations, SchemaKt.defaultSchema, config)
        then: "generated methods include all of the expected method names"
        include(generatedWorkflows, expectedGeneratedWorkflows)
        and:
        notInclude(generatedWorkflows, notExpectedGeneratedWorkflows)
    }
}
