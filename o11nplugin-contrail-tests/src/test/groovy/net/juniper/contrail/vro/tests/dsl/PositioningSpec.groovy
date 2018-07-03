/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.dsl.WorkflowBuilderKt
import net.juniper.contrail.vro.workflows.model.Position
import net.juniper.contrail.vro.workflows.model.WorkflowItemType
import spock.lang.Specification

class PositioningSpec extends Specification {

    def "Workflow items are given proper positions"() {
        when: "complex workflow is created"
            def workflow = SomeWorkflowKt.someComplexWorkflow()
        then: "the workflow items have distinct positions"
            def positions = workflow.workflowItems.collect { it.position }
            def distinctPositions = positions.unique()
            assert positions.size() == distinctPositions.size()
    }

    def "workflow items in simple workflow are placed on the one line"() {
        when: "simple workflow with script is created"
            def workflow = SomeWorkflowKt.someSimpleWorkflow()
        then: "the workflow items have the same vertical position"
            def scriptY = workflow.workflowItems.find { it.type == WorkflowItemType.task.name() }.position.y
            def endY = workflow.workflowItems.find { it.type == WorkflowItemType.end.name() }.position.y
            assert scriptY == TestUtilsKt.trueVerticalPosition(WorkflowItemType.task).toString()
            assert endY == TestUtilsKt.trueVerticalPosition(WorkflowItemType.end).toString()
    }

}