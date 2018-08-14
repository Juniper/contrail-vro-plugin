/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.workflows.util.DslUtilsKt

class SimpleWorkflowsGeneratorSpec extends GeneratorSpec{

    def "expected simple workflows are generated"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName].toSet()

        def config = createConfig(modelClasses: modelClasses)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork), DslUtilsKt.deleteWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "declared custom workflows are not generated"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName].toSet()
        def customEditWorkflows = [VirtualNetwork.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customEditWorkflows: customEditWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(VirtualNetwork)]
        def notExpectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "workflow 'create virtual network' is generated when Project is included in modelClasses"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(Project),
                                          DslUtilsKt.createSimpleWorkflowName(VirtualNetwork),
                                          DslUtilsKt.createSimpleWorkflowName(Project),
                                          DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.editWorkflowName(Project)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }
}
