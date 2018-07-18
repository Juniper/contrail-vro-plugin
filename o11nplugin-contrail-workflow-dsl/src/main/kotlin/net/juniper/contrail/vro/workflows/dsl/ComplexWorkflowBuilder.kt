/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.config.constants.basePackageName
import net.juniper.contrail.vro.workflows.model.AttributeDefinition
import net.juniper.contrail.vro.workflows.model.Bind
import net.juniper.contrail.vro.workflows.model.Binding
import net.juniper.contrail.vro.workflows.model.DefaultConditionDefinition
import net.juniper.contrail.vro.workflows.model.EqualsConditionDefinition
import net.juniper.contrail.vro.workflows.model.Parameter
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.WorkflowItemDefinition
import net.juniper.contrail.vro.workflows.model.bindAttributes
import net.juniper.contrail.vro.workflows.model.existsConnectedToEnd
import net.juniper.contrail.vro.workflows.model.replaceEndLabels
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.toFullItemId
import net.juniper.contrail.vro.workflows.util.generateID

@WorkflowBuilder
class ComplexWorkflowBuilder(
    private val workflowDefinitions: List<WorkflowDefinition>,
    val items: MutableList<WorkflowItemDefinition> = mutableListOf(),
    val attributes: MutableList<AttributeDefinition> = mutableListOf(),
    val outputParameters: MutableList<ParameterInfo> = mutableListOf()
) {
    private var baseFreeId = 1000000
    private fun nextId(): Int {
        baseFreeId += 1
        return baseFreeId
    }

    fun <T : Any> attribute (attributeName: String, attributeType: ParameterType<T>, description: String? = null) {
        attributes.add(AttributeDefinition(attributeName, attributeType, description))
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
        attributes.add(AttributeDefinition(decisionInput, string))

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
        val conditions = options.map {
            EqualsConditionDefinition(decisionInput, it.name, string, it.targetId.toFullItemId)
        } + DefaultConditionDefinition(defaultOut.toFullItemId)

        items.add(switchWorkflowItem(
            switchId,
            Binding(listOf(Bind(decisionInput, string, decisionInput))),
            conditions
        ))
    }

    fun automaticWorkflowOutput(setup: OutputAggregator.() -> Unit) {
        val itemsConnectedToEnd = items.filter { it.isConnectedToEnd() }
        val itemsWithConditionToEnd = items.filter { it.withConditionToEnd() }
        if (itemsConnectedToEnd.isEmpty() && itemsWithConditionToEnd.isEmpty()) throw IllegalStateException("There are no workflowItems connected to EndItem")

        val itemId = nextId()

        val newItems = items.map {
            when {
                it.isConnectedToEnd() -> it.copy(outItemId = itemId)
                it.withConditionToEnd() -> it.copy(conditions = it.conditions!!.replaceEndLabels(itemId))
                else -> it
            }
        }

        items.clear()
        items.addAll(newItems)
        workflowOutput(itemId, setup)
    }

    private fun workflowOutput(itemId: Int, setup: OutputAggregator.() -> Unit) {
        val aggregator = OutputAggregator().apply(setup)
        val outputs = aggregator.outputs
        val attributeNames = attributes.map { it.name }
        val outputAttributeNames = outputs.keys
        if (!outputAttributeNames.all { it in attributeNames }) throw IllegalArgumentException("Given attributes don't exist in this workflow")

        val attributesToBind = attributes.filter { it.name in outputAttributeNames }
        attributesToBind.forEach { outputParameters.add(ParameterInfo(outputs[it.name]!!, it.type)) }
        val inputBinds = Binding(attributesToBind.map { Bind(it.name, it.type, it.name, "" ) })
        val outputBinds = Binding(attributesToBind.map { Bind(it.name, it.type, outputs[it.name]!!, "") })

        items.add(scriptWorkflowItem(
            itemId,
            Script(""),
            inputBinds,
            outputBinds,
            workflowEndItemId
        ))
    }

    fun attributeInput(itemId: Int, outItemId: Int, parameterDefinitions: ParameterAggregator.() -> Unit = {}) {
        if (itemId > baseFreeId) throw IllegalArgumentException("Use ID lower than $baseFreeId")

        val parameters = mutableListOf<ParameterInfo>()
        val allParameters = mutableListOf<ParameterInfo>()
        ParameterAggregator(parameters, allParameters).apply(parameterDefinitions)

        val attributeNames = attributes.map { it.name }
        val paramNames = parameters.map { it.name }
        if (!paramNames.all { it in attributeNames }) throw IllegalArgumentException("Entered parameter doesn't exist in Workflow")

        val outputBinding = Binding(parameters.asBinds)

        items.add(inputWorkflowItem(
            itemId,
            Binding(listOf()),
            outputBinding,
            outItemId,
            null,
            parameterDefinitions
        ))
    }

    class WorkflowInvocationParameters(
        val workflowInputBinding: Binding,
        val workflowOutputBinding: Binding,
        val newAttributes: List<AttributeDefinition>
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
                it.asAttributeDefinition(attributeName)
            }
        val newOutputAttributes =
            unboundOutputParams.map {
                val attributeName = attributeName(attributePrefix, it.name)
                it.asAttributeDefinition(attributeName)
            }

        return WorkflowInvocationParameters(
            workflowInputBinding,
            workflowOutputBinding,
            newInputAttributes + newOutputAttributes
        )
    }

    private fun attributeName(prefix: String, rawName: String): String = "$prefix$rawName"

    private fun Parameter.asBindWithExportName(exportName: String) = toParameterInfo().asBindWithExportName(exportName)

    private fun Parameter.asAttributeDefinition(attributeName: String): AttributeDefinition {
        val parameterInfo = toParameterInfo()
        return AttributeDefinition(
            attributeName,
            parameterInfo.type,
            parameterInfo.description
        )
    }

    private fun WorkflowItemDefinition.isConnectedToEnd() = outItemId == workflowEndItemId

    private fun WorkflowItemDefinition.withConditionToEnd() = conditions != null && conditions.existsConnectedToEnd
}
