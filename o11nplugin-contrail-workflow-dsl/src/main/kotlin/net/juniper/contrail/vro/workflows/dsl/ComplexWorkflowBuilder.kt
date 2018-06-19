/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.Attribute
import net.juniper.contrail.vro.workflows.model.Bind
import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.DefaultCondition
import net.juniper.contrail.vro.workflows.model.EqualsCondition
import net.juniper.contrail.vro.workflows.model.WorkflowItem
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.generateID

@WorkflowBuilder
class ComplexWorkflowBuilder(
    private val items: MutableList<WorkflowItem>,
    private val attributes: MutableList<Attribute>,
    private val workflowDefinitions: List<WorkflowDefinition>
) {
    // TODO: make it generate some unique id (we should pass the ID as string (at least to the most intimate methods) so that we can use some special ID for auxiliary objects)
    private var itemId = 123456
    private fun randomId(): Int {
        itemId += 1
        return itemId
    }

    // TODO: allow the user to connect inputs to attributes
    // inputMapping["inputParameter"] = "majorWorkflowAttribute"
    // !! ADD OUT-BINDINGS SO THAT RESULTS OF WORKFLOWS CAN BE STORED IN ATTRIBUTES
    fun workflowInvokation(itemId: Int, workflowPackage: String, workflowName: String, outItemId: Int, inputMapping: Map<String, String>) {
        // TODO: the same code is used in JsTester, it should be somehow unified
        val definition = workflowDefinitions.find { it.displayName == workflowName } ?: throw IllegalArgumentException()

        println("--- TEST ---")
        println(definition)
        println(definition.input.parameters)
        println(definition.output.parameters)
        println(definition.presentation)
        println(definition.workflowItems)
        println("--- TEST ---")

        val workflowId = generateID(workflowPackage, workflowName)
        val workflowItemId = randomId()
        val allParams = definition.input.parameters
        val externalParams = allParams.filter { inputMapping.containsKey(it.name) }
        val inputParams = allParams.filter { !inputMapping.containsKey(it.name) }
        val inputParamBindings = Binding(inputParams.map { it.toParameterInfo().asBind })

        val presentation = definition.presentation

        // TODO: process presentation

        // TODO: Add bindigns to both Input and Workflow items
        // TODO: add presentation directly
        items.add(inputWorkflowItem(
            itemId,
            Binding(listOf()),
            inputParamBindings,
            workflowItemId
        ) {})

        items.add(workflowWorkflowItem(
            workflowItemId,
            workflowId,
            Binding(listOf()),
            Binding(listOf()),
            outItemId
        ))
    }

    // TODO: setup method instead of options argument? Or at least a class instead of List<Pair<String,String>>
    fun choice(itemId: Int, title: String, options: List<Pair<String, String>>, defaultOut: String) {
        val decisionInput = "Decision_$itemId"
        attributes.add(Attribute(decisionInput, string))
        val switchId = randomId()
        items.add(inputWorkflowItem(
            itemId,
            Binding(listOf()),
            Binding(listOf(Bind(decisionInput, string, decisionInput))),
            switchId
        ) {
            parameter(decisionInput, string) {
                mandatory = true
                predefinedAnswers = options.map { it.first }
            }
        })
        items.add(switchWorkflowItem(
            switchId,
            Binding(listOf(Bind(decisionInput, string, decisionInput))),
            options.map {
                EqualsCondition(decisionInput, it.first, string, it.second)
            } + DefaultCondition(defaultOut)
        ))
    }
}