/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.vro.tests.TestUtilsKt
import net.juniper.contrail.vro.workflows.util.DslUtilsKt

class SimpleWorkflowsGeneratorSpec extends GeneratorSpec{

    def "top level models will have create, edit and delete workflows generated"() {
        given:
        def modelClasses = [topLevelModel.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(topLevelModel),
                                          DslUtilsKt.createSimpleWorkflowName(topLevelModel),
                                          DslUtilsKt.editWorkflowName(topLevelModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "non-top level models will have edit and delete workflows generated"() {
        given:
        def modelClasses = [nonTopLevelModel.simpleName].toSet()

        def config = createConfig(modelClasses: modelClasses)

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(nonTopLevelModel), DslUtilsKt.deleteWorkflowName(nonTopLevelModel)]
        def notExpectedGeneratedWorkflows = [DslUtilsKt.createSimpleWorkflowName(nonTopLevelModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include create method"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "non-top level models will have create, edit and delete workflows generated if we add its model parent"() {
        given:
        def (parent, child) = parentChildPair
        def modelClasses = [parent.simpleName, child.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(child),
                                          DslUtilsKt.deleteWorkflowName(child),
                                          DslUtilsKt.createSimpleWorkflowName(child)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customEditWorkflows results in not generating edit workflow"() {
        given:
        def modelClasses = [nonTopLevelModel.simpleName].toSet()
        def customEditWorkflows = [nonTopLevelModel.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customEditWorkflows: customEditWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(nonTopLevelModel)]
        def notExpectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(nonTopLevelModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding model to customDeleteWorkflows results in not generating delete workflow"() {
        given:
        def modelClasses = [nonTopLevelModel.simpleName].toSet()
        def customDeleteWorkflows = [nonTopLevelModel.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customDeleteWorkflows: customDeleteWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(nonTopLevelModel)]
        def notExpectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(nonTopLevelModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding workflow to customCreateWorkflows results in not generating create workflow"() {
        given:
        def modelClasses = [topLevelModel.simpleName].toSet()
        def customCreateWorkflows = [topLevelModel.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customCreateWorkflows: customCreateWorkflows
        )

        def notExpectedGeneratedWorkflows = [DslUtilsKt.createSimpleWorkflowName(topLevelModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods does not include custom method names"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding workflow to customCreateWorkflows does not affect generating other workflows"() {
        given:
        def (parent, child) = parentChildPair
        def modelClasses = [child.simpleName, parent.simpleName].toSet()
        def customCreateWorkflows = [child.simpleName, parent.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customCreateWorkflows: customCreateWorkflows
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(child),
                                          DslUtilsKt.editWorkflowName(parent),
                                          DslUtilsKt.deleteWorkflowName(child),
                                          DslUtilsKt.deleteWorkflowName(parent)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.createSimpleWorkflowName(child),
                                             DslUtilsKt.createSimpleWorkflowName(parent)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding models which are in relation results in generating reference workflows"() {
        given:
        def (referencer, referenced) = relatedPair
        def modelClasses = [referencer.simpleName, referenced.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(referencer, referenced),
                                          DslUtilsKt.removeRelationWorkflowName(referencer, referenced)]

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding models which are in relation does not affect generating other workflows"() {
        given:
        def (referencer, referenced) = relatedPair
        println(referencer)
        def modelClasses = [referencer.simpleName, referenced.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        def expectedGeneratedWorkflows = [DslUtilsKt.deleteWorkflowName(referencer),
                                          DslUtilsKt.editWorkflowName(referencer),
                                          DslUtilsKt.deleteWorkflowName(referenced)]

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding models which are not in relation does not result in generating reference workflows"() {
        given:
        def (someModel, anotherModel) = unrelatedPair
        def modelClasses = [someModel.simpleName, anotherModel.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        def notExpectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(someModel, anotherModel),
                                             DslUtilsKt.addRelationWorkflowName(anotherModel, someModel),
                                             DslUtilsKt.removeRelationWorkflowName(someModel, anotherModel),
                                             DslUtilsKt.removeRelationWorkflowName(anotherModel, someModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods does not include all of the reference method names"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding models which are not in relation does not affect generating other workflows"() {
        given:
        def (someModel, anotherModel) = unrelatedPair
        def modelClasses = [someModel.simpleName, anotherModel.simpleName].toSet()

        def config = createConfig(
            modelClasses: modelClasses
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(someModel),
                                          DslUtilsKt.editWorkflowName(anotherModel),
                                          DslUtilsKt.deleteWorkflowName(someModel),
                                          DslUtilsKt.deleteWorkflowName(anotherModel)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customAddReference results in not generating add reference workflow"() {
        given:
        def (referencer, referenced) = relatedPair
        def modelClasses = [referencer.simpleName, referenced.simpleName].toSet()
        def customAddReference = [TestUtilsKt.getPairOf(referencer, referenced)].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customAddReference: customAddReference
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.removeRelationWorkflowName(referencer, referenced)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(referencer, referenced)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding model to customAddReference does not affect generating other workflows"() {
        given:
        def (referencer, referenced) = relatedPair
        def modelClasses = [referencer.simpleName, referenced.simpleName].toSet()
        def customAddReference = [TestUtilsKt.getPairOf(referencer, referenced)].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customAddReference: customAddReference
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(referencer),
                                          DslUtilsKt.deleteWorkflowName(referencer)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }

    def "adding model to customRemoveReference results in not generating remove reference workflow"() {
        given:
        def (referencer, referenced) = relatedPair
        def modelClasses = [referencer.simpleName, referenced.simpleName].toSet()
        def customRemoveReference = [TestUtilsKt.getPairOf(referencer, referenced)].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customRemoveReference: customRemoveReference
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.addRelationWorkflowName(referencer, referenced)]

        def notExpectedGeneratedWorkflows = [DslUtilsKt.removeRelationWorkflowName(referencer, referenced)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
        and: "generated methods does not include custom methods"
        notInclude(generatedWorkflowNames, notExpectedGeneratedWorkflows)
    }

    def "adding model to customRemoveReference does not affect generating other workflows"() {
        given:
        def (referencer, referenced) = relatedPair
        def modelClasses = [referencer.simpleName, referenced.simpleName].toSet()
        def customRemoveReference = [TestUtilsKt.getPairOf(referencer, referenced)].toSet()

        def config = createConfig(
            modelClasses: modelClasses,
            customRemoveReference: customRemoveReference
        )

        def expectedGeneratedWorkflows = [DslUtilsKt.editWorkflowName(referencer),
                                          DslUtilsKt.deleteWorkflowName(referencer)]

        when: "generating workflows"
        def generatedWorkflowNames = generateSimpleWorkflows(config)

        then: "generated methods include all of the expected method names"
        include(generatedWorkflowNames, expectedGeneratedWorkflows)
    }
}
