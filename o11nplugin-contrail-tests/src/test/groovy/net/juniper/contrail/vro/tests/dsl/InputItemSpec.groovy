/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import spock.lang.Specification

class AddWorkflowInputItemSpec extends Specification {

    def "InputItem with given attributes is created"() {
        given:
            def attributeNames = ["attribute1", "attribute2", "attribute3", "attribute4"]
        when: "Workflow is created"
            def workflow = SomeWorkflowKt.someWorkflowWithInputItem(attributeNames)
        then: "InputItem with correct attributes is in Workflow"
            def items = workflow.workflowItems
            def inputItem = items.find {it.name == "item" + SomeWorkflowKt.start}
            def paramNames = inputItem.presentation.presentationParameters.collect {it.name}
            attributeNames.every { it in paramNames }
    }
}
