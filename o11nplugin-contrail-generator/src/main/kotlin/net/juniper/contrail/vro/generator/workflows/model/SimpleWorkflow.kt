/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

fun Workflow.addSingleScript(scriptBody: String, setup: SimpleWorkflowBuilder.() -> Unit): Workflow {
    val script = scriptWorkflowItem(1, scriptBody)
    workflowItems.add(END)
    workflowItems.add(script)
    output {
        parameter("success", boolean)
    }
    script.outBinding("success", boolean)
    val builder = SimpleWorkflowBuilder(this, script)
    builder.setup()
    return this
}

class SimpleWorkflowBuilder(private val workflow: Workflow, private val script: WorkflowItem) {

    fun step(title: String? = null, setup: SimpleWorkflowStepBuilder.() -> Unit) {
        val step = PresentationStep(title)
        val builder = SimpleWorkflowStepBuilder(workflow, script, step)
        builder.setup()
        workflow.presentation.addStep(step)
    }
}

@Suppress("UNUSED_PARAMETER")
class SimpleWorkflowStepBuilder(
    private val workflow: Workflow,
    private val script: WorkflowItem,
    private val step: PresentationStep) {

    @JvmName("booleanParameter")
    fun parameter(name: String, type: boolean, setup: BooleanParameterBuilder.() -> Unit) =
        BooleanParameterBuilder(name).updateWith(setup)

    @JvmName("intParameter")
    fun parameter(name: String, type: number, setup: IntParameterBuilder.() -> Unit) =
        IntParameterBuilder(name).updateWith(setup)

    @JvmName("stringParameter")
    fun parameter(name: String, type: string, setup: StringParameterBuilder.() -> Unit) =
        StringParameterBuilder(name).updateWith(setup)

    @JvmName("secureStringParameter")
    fun parameter(name: String, type: SecureString, setup: StringParameterBuilder.() -> Unit) =
        StringParameterBuilder(name).updateWith(setup)

    @JvmName("referenceParameter")
    fun parameter(name: String, type: Reference, setup: ReferenceParameterBuilder.() -> Unit) =
        //TODO add reference to workflow
        ReferenceParameterBuilder(name, type).updateWith(setup)

    private fun <Builder : BasicParameterBuilder<T>, T : Any> Builder.updateWith(setup: Builder.() -> Unit) =
        apply(setup).updateWorkflow()

    private fun BasicParameterBuilder<*>.updateWorkflow() {
        val parameter = createPresentationParameter()
        workflow.input.addParameter(Parameter(name, type, parameter.description))
        step.addParameter(parameter)
        script.inBinding(name, type)
    }
}

private inline fun <T : Any> T?.ifDefined(block: T.() -> Unit) =
    this?.block()

abstract class BasicParameterBuilder<Type: Any>(val name: String, val type: ParameterType<Type>) {
    var description: String = name
    var mandatory: Boolean = false
    var defaultValue: Type? = null

    fun createPresentationParameter() = PresentationParameter (
        name = name,
        description = description,
        qualifiers = allQualifiers
    )

    private val allQualifiers get(): List<ParameterQualifier> =
        commonQualifiers + customQualifiers

    private val commonQualifiers get() = mutableListOf<ParameterQualifier>().apply {
        if (mandatory) add(mandatoryQualifier)
        defaultValue.ifDefined { add(defaultValueQualifier(type, this)) }
    }

    protected open val customQualifiers: List<ParameterQualifier> get() =
        emptyList()
}

class BooleanParameterBuilder(name: String) : BasicParameterBuilder<Boolean>(name, boolean)

class IntParameterBuilder(name: String) : BasicParameterBuilder<Int>(name, number) {
    var min: Int? = null
    var max: Int? = null

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        qualifiers.add(numberFormatQualifier("#0"))
        min.ifDefined {
            qualifiers.add(minNumberValueQualifier(this))
        }
        max.ifDefined {
            qualifiers.add(maxNumberValueQualifier(this))
        }
        return qualifiers
    }
}

class StringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, string)
class SecureStringParameterBuilder(name: String) : BasicParameterBuilder<String>(name, SecureString)

class ReferenceParameterBuilder(name: String, type: Reference) : BasicParameterBuilder<Reference>(name, type) {

    var showInInventory: Boolean = false

    override val customQualifiers get(): List<ParameterQualifier> {
        val qualifiers = mutableListOf<ParameterQualifier>()
        if (showInInventory) qualifiers.add(showInInventoryQualifier)
        return qualifiers
    }
}
