/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.tests.dsl.SomeWorkflowKt
import spock.lang.Specification

class AddWorkflowInputItemSpec extends Specification {

    def "InputItem with given attribues is created"() {
        given:
            def attributeNames = SomeWorkflowKt.attributeNames
        when: "Workflow is created"
            def workflow = SomeWorkflowKt.someWorkflowWithInputItem()
        then: "InputItem with correct attributes is in Workflow"
            def items = workflow.workflowItems
            def inputItem = items.find {it.name == "item" + SomeWorkflowKt.start}
            def paramNames = inputItem.presentation.presentationParameters.collect {it.name}
            attributeNames.every { it in paramNames }
    }
}
