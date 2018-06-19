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
import net.juniper.contrail.vro.workflows.model.Parameter
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.WorkflowItem
import net.juniper.contrail.vro.workflows.model.bindAttributes
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.toFullItemId
import net.juniper.contrail.vro.workflows.util.generateID

// TODO: POSITIONS (right now all objects are stacked on top of one another)
// TODO: Attributes input (set values of global attributes without invoking any workflow)
// TODO: Output parameters (I see no way to do this other than dummy script object with relevant bindings)
@WorkflowBuilder
class ComplexWorkflowBuilder(
    private val workflowDefinitions: List<WorkflowDefinition>,
    val items: MutableList<WorkflowItem> = mutableListOf(),
    val attributes: MutableList<Attribute> = mutableListOf()
) {
    private var baseFreeId = 1000000
    private fun nextId(): Int {
        baseFreeId += 1
        return baseFreeId
    }

    fun <T : Any> attribute (attributeName: String, attributeType: ParameterType<T>, description: String? = null) {
        attributes.add(Attribute(attributeName, attributeType, description))
    }

    fun workflowInvocation(itemId: Int, outItemId: Int, workflowName: String, workflowPackage: String = basePackageName, setup: BindAggregator.() -> Unit) {
        if (itemId > baseFreeId) throw IllegalArgumentException("Use ID lower than $baseFreeId")
        val definition = workflowDefinitions.find { it.displayName == workflowName } ?: throw IllegalArgumentException()

        val attributePrefix = "workflowItem$itemId"

        val workflowId = generateID(workflowPackage, workflowName)

        val aggregator = BindAggregator().apply(setup)
        val inputMapping = aggregator.inBinds
        val outputMapping = aggregator.outBinds

        val inputParams = definition.input.parameters
        val outputParams = definition.output.parameters

        val invocationParameters = prepareWorkflowInvocationParameters(
            inputParams,
            outputParams,
            inputMapping,
            outputMapping,
            attributePrefix
        )
        val newAttributes = invocationParameters.newAttributes
        val workflowInputBinding = invocationParameters.workflowInputBinding
        val workflowOutputBinding = invocationParameters.workflowOutputBinding

        attributes.addAll(newAttributes)

        val presentation = definition.presentation.bindAttributes(inputMapping)

        val workflowItemId = if (inputParams.any { !inputMapping.containsKey(it.name) }) {
            val nextId = nextId()
            items.add(inputWorkflowItem(
                itemId,
                Binding(listOf()),
                workflowInputBinding,
                nextId,
                presentation
            ))
            nextId
        } else {
            itemId
        }

        items.add(workflowWorkflowItem(
            workflowItemId,
            workflowName,
            workflowId,
            workflowInputBinding,
            workflowOutputBinding,
            outItemId
        ))
    }

    fun choice(itemId: Int, defaultOut: Int, title: String, setup: ChoiceAggregator.() -> Unit) {
        if (itemId > baseFreeId) throw IllegalArgumentException("Use ID lower than $baseFreeId")
        val switchId = nextId()

        val aggregator = ChoiceAggregator().apply(setup)
        val options = aggregator.choices

        val decisionInput = "Decision_$itemId"
        attributes.add(Attribute(decisionInput, string))

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

    class WorkflowInvocationParameters(
        val workflowInputBinding: Binding,
        val workflowOutputBinding: Binding,
        val newAttributes: List<Attribute>
    )

    private fun prepareWorkflowInvocationParameters(
        inputParams: List<Parameter>,
        outputParams: List<Parameter>,
        inputMapping: Map<String, String>,
        outputMapping: Map<String, String>,
        attributePrefix: String): WorkflowInvocationParameters {

        val boundInputParams = inputParams.filter { inputMapping.containsKey(it.name) }
        val unboundInputParams = inputParams.filter { !inputMapping.containsKey(it.name) }
        val boundOutputParams = outputParams.filter { outputMapping.containsKey(it.name) }
        val unboundOutputParams = outputParams.filter { !outputMapping.containsKey(it.name) }

        val externalBinds =
            boundInputParams.map {
                val exportName = inputMapping[it.name]!!
                it.asBindWithExportName(exportName)
            }
        val interactionParamBinds =
            unboundInputParams.map {
                val exportName = attributeName(attributePrefix, it.name)
                it.asBindWithExportName(exportName)
            }

        val workflowInputBinding = Binding(interactionParamBinds + externalBinds)
        val workflowOutputBinding = Binding(
            boundOutputParams.map {
                val exportName = outputMapping[it.name]!!
                it.asBindWithExportName(exportName)
            })

        val newInputAttributes =
            unboundInputParams.map {
                val attributeName = attributeName(attributePrefix, it.name)
                it.asAttribute(attributeName)
            }
        val newOutputAttributes =
            unboundOutputParams.map {
                val attributeName = attributeName(attributePrefix, it.name)
                it.asAttribute(attributeName)
            }

        return WorkflowInvocationParameters(
            workflowInputBinding,
            workflowOutputBinding,
            newInputAttributes + newOutputAttributes
        )
    }

    private fun attributeName(prefix: String, rawName: String): String = "$prefix$rawName"

    private fun Parameter.asBindWithExportName(exportName: String) = toParameterInfo().asBindWithExportName(exportName)

    private fun Parameter.asAttribute(attributeName: String): Attribute {
        val parameterInfo = toParameterInfo()
        return Attribute(
            attributeName,
            parameterInfo.type,
            parameterInfo.description
        )
    }
}