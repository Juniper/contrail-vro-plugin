/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.config.constants.basePackageName
import net.juniper.contrail.vro.workflows.model.Attribute
import net.juniper.contrail.vro.workflows.model.Bind
import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.DefaultCondition
import net.juniper.contrail.vro.workflows.model.EqualsCondition
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.WorkflowItem
import net.juniper.contrail.vro.workflows.model.bindAttributes
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.toFullItemId
import net.juniper.contrail.vro.workflows.util.generateID

// TODO: POSITIONS (right now all objects are stacked on top of one another)
// TODO: Attributes input (set values of global attributes without invoking any workflow)
// TODO: Output parameters (I see no way to do this other than dummy script object with relevant bindings
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

    fun <T : Any> attribute (attributeName: String, attributeType: ParameterType<T>, description: String? = null) {
        attributes.add(Attribute(attributeName, attributeType, description))
    }

    // TODO: Maps can be changed into some aggregator method that accepts 'input' and 'output' declarations
    fun workflowInvokation(itemId: Int, outItemId: Int, workflowName: String, workflowPackage: String = basePackageName, setup: BindAggregator.() -> Unit) {
        // TODO: the same code is used in JsTester, it could be somehow unified
        val definition = workflowDefinitions.find { it.displayName == workflowName } ?: throw IllegalArgumentException()

        val attributePrefix = "workflowItem$itemId"

        val workflowId = generateID(workflowPackage, workflowName)
        val allInputParams = definition.input.parameters
        val outputParams = definition.output.parameters

        val inputMapping: MutableMap<String, String> = mutableMapOf()
        val outputMapping: MutableMap<String, String> = mutableMapOf()
        BindAggregator(inputMapping, outputMapping).setup()

        val externalParams = allInputParams.filter { inputMapping.containsKey(it.name) }
        val interactionParams = allInputParams.filter { !inputMapping.containsKey(it.name) }
        val interactionParamBindings = Binding(interactionParams.map { it.toParameterInfo().asBindWithExportName(attributeName(attributePrefix, it.name)) })

        val workflowOutputBindings = Binding(outputParams.filter { outputMapping.containsKey(it.name) }.map { it.toParameterInfo().asBindWithExportName(outputMapping[it.name]!!) })

        val newInputAttributes = interactionParams.map { it.toParameterInfo() }.map { Attribute(attributeName(attributePrefix, it.name), it.type, it.description) }
        val newOutputAttributes = outputParams.filter { !outputMapping.containsKey(it.name) }.map { it.toParameterInfo() }.map { Attribute(attributeName(attributePrefix, it.name), it.type, it.description) }

        val externalBindings = Binding(externalParams.map { it.toParameterInfo().asBindWithExportName(inputMapping[it.name]!!) })
        val allWorkflowInputBindings = Binding(interactionParamBindings.binds + externalBindings.binds)

        attributes.addAll(newInputAttributes + newOutputAttributes)

        // It should work alright if there is no input item, because we bind workflow parameters straight to workflow attributes
        val presentation = definition.presentation.bindAttributes(inputMapping)

        if (interactionParamBindings.binds.isNotEmpty()) {
            val workflowItemId = randomId()
            items.add(inputWorkflowItem(
                itemId,
                Binding(listOf()),
                allWorkflowInputBindings,
                workflowItemId,
                presentation
            ))

            items.add(workflowWorkflowItem(
                workflowItemId,
                workflowName,
                workflowId,
                allWorkflowInputBindings,
                workflowOutputBindings,
                outItemId
            ))
        } else {
            items.add(workflowWorkflowItem(
                itemId,
                workflowName,
                workflowId,
                allWorkflowInputBindings,
                workflowOutputBindings,
                outItemId
            ))
        }
    }

    private fun attributeName(prefix: String, rawName: String): String = "$prefix$rawName"

    fun choice(itemId: Int, defaultOut: Int, title: String, setup: ChoiceAggregator.() -> Unit) {
        val decisionInput = "Decision_$itemId"
        attributes.add(Attribute(decisionInput, string))
        val switchId = randomId()

        val options = mutableListOf<Choice>()
        ChoiceAggregator(options).setup()

        items.add(inputWorkflowItem(
            itemId,
            Binding(listOf()),
            Binding(listOf(Bind(decisionInput, string, decisionInput))),
            switchId
        ) {
            parameter(decisionInput, string) {
                description = title
                mandatory = true
                predefinedAnswers = options.map { it.name }
            }
        })
        items.add(switchWorkflowItem(
            switchId,
            Binding(listOf(Bind(decisionInput, string, decisionInput))),
            options.map {
                EqualsCondition(decisionInput, it.name, string, it.targetId.toFullItemId)
            } + DefaultCondition(defaultOut.toFullItemId)
        ))
    }
}