/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.workflows.util.DslUtilsKt

class SimpleWorkflowsGeneratorSpec extends GeneratorSpec{

    def "adding model to modelClasses results in generting simple workflows for this model"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName].toSet()

        def config = createConfig(modelClasses: modelClasses)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork), DslUtilsKt.deleteWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customEditWorkflows results in not generating edit workflow"() {
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

    def "adding model to customDeleteWorkflows results in not generating delete workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()
        def customDeleteWorkflows = [VirtualNetwork.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customDeleteWorkflows: customDeleteWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(Project),
                                          DslUtilsKt.createSimpleWorkflowName(VirtualNetwork),
                                          DslUtilsKt.createSimpleWorkflowName(Project),
                                          DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.editWorkflowName(Project)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding workflow to customCreateWorkflows results in not generating create workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()
        def customCreateWorkflows = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()

        def config = createConfig(
                modelClasses: modelClasses,
                customCreateWorkflows: customCreateWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(Project),
                                          DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.editWorkflowName(Project)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.createSimpleWorkflowName(VirtualNetwork),
                                             DslUtilsKt.createSimpleWorkflowName(Project)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "providing models which are in relation results in generating reference workflows"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, NetworkPolicy.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(NetworkPolicy),
                                          DslUtilsKt.addRelationWorkflowName(VirtualNetwork, NetworkPolicy),
                                          DslUtilsKt.removeRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customAddReference results in not generating add reference workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, NetworkPolicy.class.simpleName].toSet()
        def customAddReference = [GeneratorUtilsKt.getVirtualNetworkPolicyPair()].toSet()

        def config = createConfig(
                modelClasses: modelClasses,
                customAddReference: customAddReference
        )

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(NetworkPolicy),
                                          DslUtilsKt.removeRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding model to customRemoveReference results in not generating remove reference workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, NetworkPolicy.class.simpleName].toSet()
        def customRemoveReference = [GeneratorUtilsKt.getVirtualNetworkPolicyPair()].toSet()

        def config = createConfig(
                modelClasses: modelClasses,
                customRemoveReference: customRemoveReference
        )

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(NetworkPolicy),
                                          DslUtilsKt.addRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.removeRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

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
