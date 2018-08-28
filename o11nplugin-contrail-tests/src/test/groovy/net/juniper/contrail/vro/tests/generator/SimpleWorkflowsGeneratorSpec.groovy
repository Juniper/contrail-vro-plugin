/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.tests.TestUtilsKt
import net.juniper.contrail.vro.workflows.util.DslUtilsKt

class SimpleWorkflowsGeneratorSpec extends GeneratorSpec{

    def "model without model parent will have create, edit and delete workflows generated"() {
        given:
        def modelClasses = [Project.class.simpleName].toSet()

        def config = createConfig(
                modelClasses: modelClasses
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(Project),
                                          DslUtilsKt.createSimpleWorkflowName(Project),
                                          DslUtilsKt.editWorkflowName(Project)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "model with model parent will have edit and delete workflows generated"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName].toSet()

        def config = createConfig(modelClasses: modelClasses)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork), DslUtilsKt.deleteWorkflowName(VirtualNetwork)]
        def notExpectedGeneratedWorkflows = [DslUtilsKt.createSimpleWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include create method"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "model with model parent will have create, edit and delete workflows generated if we add its model parent"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()

        def config = createConfig(modelClasses: modelClasses)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(VirtualNetwork),
                                          DslUtilsKt.deleteWorkflowName(VirtualNetwork),
                                          DslUtilsKt.createSimpleWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customEditWorkflows does not result in generating edit workflow"() {
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

    def "adding model to customDeleteWorkflows does not result in generating delete workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()
        def customDeleteWorkflows = [VirtualNetwork.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customDeleteWorkflows: customDeleteWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(Project)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(VirtualNetwork)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding workflow to customCreateWorkflows does not result in generating create workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()
        def customCreateWorkflows = [VirtualNetwork.class.simpleName, Project.class.simpleName].toSet()

        def config = createConfig(
                modelClasses: modelClasses,
                customCreateWorkflows: customCreateWorkflows
        )

        def notExpectedGeneratedWorkflows = [DslUtilsKt.createSimpleWorkflowName(VirtualNetwork),
                                             DslUtilsKt.createSimpleWorkflowName(Project)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods does not include custom method names"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding models which are not in relation does not result in generating reference workflows"() {
        given:
        def modelClasses = [NetworkIpam.class.simpleName, SecurityGroup.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        def notExpectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(SecurityGroup, NetworkIpam),
                                             DslUtilsKt.addRelationWorkflowName(NetworkIpam, SecurityGroup),
                                             DslUtilsKt.removeRelationWorkflowName(SecurityGroup, NetworkIpam),
                                             DslUtilsKt.removeRelationWorkflowName(NetworkIpam, SecurityGroup)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods does not include all of the reference method names"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding models which are in relation results in generating reference workflows"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, NetworkPolicy.class.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(VirtualNetwork, NetworkPolicy),
                                          DslUtilsKt.removeRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customAddReference does not result in generating add reference workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, NetworkPolicy.class.simpleName].toSet()
        def customAddReference = [TestUtilsKt.getPairOf(VirtualNetwork, NetworkPolicy)].toSet()

        def config = createConfig(
                modelClasses: modelClasses,
                customAddReference: customAddReference
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.removeRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding model to customRemoveReference does not result in generating remove reference workflow"() {
        given:
        def modelClasses = [VirtualNetwork.class.simpleName, NetworkPolicy.class.simpleName].toSet()
        def customRemoveReference = [TestUtilsKt.getPairOf(VirtualNetwork, NetworkPolicy)].toSet()

        def config = createConfig(
                modelClasses: modelClasses,
                customRemoveReference: customRemoveReference
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.removeRelationWorkflowName(VirtualNetwork, NetworkPolicy)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }
}
