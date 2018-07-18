package net.juniper.contrail.vro.tests.dsl

import spock.lang.Specification

class AutomaticOutputSpec extends Specification {

    def "Script Item is connected to all nodes previously connected to EndItem"() {
        given:
            def attributeNames = []
            def parameterNames = []
            def choices = 2
            def inputItems = 2
        when: "Complex workflow is created"
            def workflow = SomeWorkflowKt.someComplexWorkflowWithInputChoiceAutomaticOutput(choices, inputItems, attributeNames, parameterNames)
        then: "All connections previously targeting EndItem now target scriptItem"
            def scriptItem = workflow.workflowItems.find { it.type == "task" }
            def outConnections = workflow.workflowItems.collect { it.outName }
            def switchItems = workflow.workflowItems.findAll { it.type == "switch" }
            def allConditions = switchItems.collect { it.conditions }.flatten()
            def allLabels = allConditions.collect { it.label }
            def scriptConnections = (outConnections + allLabels).findAll { it == scriptItem.name }
            scriptConnections.size() == choices*2 + inputItems
    }


    def "Script Item has all relevant Bindings"() {
        given:
            def attributeNames = ["attribute1", "attribute2", "attribute3", "attribute4"]
            def parameterNames = ["parameter1", "parameter2", "parameter3", "parameter4"]
            def choices = 0
            def inputItems = 1
        when: "Complex workflow is created"
            def workflow = SomeWorkflowKt.someComplexWorkflowWithInputChoiceAutomaticOutput(choices, inputItems, attributeNames, parameterNames)
        then: "ScriptItem has correct bindings"
            def scriptItem = workflow.workflowItems.find { it.type == "task" }

            def inBindings = scriptItem.inBinding.binds
            def inBindNamesWithAttributeNames = [
                    inBindings.collect { it.name },
                    inBindings.collect { it.exportName },
                    attributeNames
            ].transpose()
            def inBindBooleans = inBindNamesWithAttributeNames.collect { it[0] == it[1] && it[1] == it[2] }

            def outBindings = scriptItem.outBinding.binds
            def outBindNameWithParameterNames = [
                    outBindings.collect { it.name },
                    outBindings.collect { it.exportName },
                    attributeNames,
                    parameterNames
            ].transpose()
            def outBindBooleans = outBindNameWithParameterNames.collect { it[0] == it[2] && it[1] == it[3] }

            inBindBooleans.every { it } && inBindBooleans.size() == attributeNames.size()
            outBindBooleans.every { it } && outBindBooleans.size() == attributeNames.size()
    }
}
