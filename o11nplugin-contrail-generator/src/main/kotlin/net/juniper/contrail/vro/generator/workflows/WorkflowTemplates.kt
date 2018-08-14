/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.generator.model.properties
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.hasOnlyListOfStrings
import net.juniper.contrail.vro.config.isPropertyListWrapper
import net.juniper.contrail.vro.config.isStringListWrapper
import net.juniper.contrail.vro.config.parameterName
import net.juniper.contrail.vro.config.constants.Contrail
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.generator.model.ClassProperties
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.AlwaysVisible
import net.juniper.contrail.vro.workflows.dsl.DataBinding
import net.juniper.contrail.vro.workflows.model.Element
import net.juniper.contrail.vro.workflows.dsl.FromBooleanParameter
import net.juniper.contrail.vro.workflows.dsl.FromComplexPropertyValue
import net.juniper.contrail.vro.workflows.dsl.NullStateOfProperty
import net.juniper.contrail.vro.workflows.dsl.VisibilityCondition
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.and
import net.juniper.contrail.vro.workflows.dsl.asValidationCondition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.createDunesProperties
import net.juniper.contrail.vro.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.workflows.model.parameterType
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.crudStatus
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.custom.hasBackrefs
import net.juniper.contrail.vro.workflows.util.deleteWorkflowName

fun Element.elementInfoPropertiesFor(categoryPath: String) = createElementInfoProperties(
    categoryPath = categoryPath,
    type = elementType,
    name = outputName,
    id = id
)

fun dunesPropertiesFor(packageName: String, version: String) = createDunesProperties(
    pkgDescription = "$Contrail package",
    pkgName = packageName,
    usedPlugins = "$Contrail#$version",
    pkgOwner = "Juniper",
    pkgId = "4452345677834623546675023032605023032"
)

val maxComplexLevel = 1
val maxPrimitiveLevel = 2

val tab = "    "
val AdvancedParameters = "Advanced parameters"
val CustomParameters = "Custom parameters"

fun deleteWorkflow(className: String, scriptBody: String) =
    workflow(deleteWorkflowName(className)).withScript(scriptBody) {
        parameter(item, className.reference) {
            description = "${className.allCapitalized} to delete"
            mandatory = true
            showInInventory = true
            validWhen = hasBackrefs()
        }
    }

val Property.parameterName get() =
    pluginPropertyName

val Property.title get() = when (propertyName) {
    "mgmt" -> "Configuration"
    "virtualMachineInterface" -> "Port"
    "ecmpHashingIncludeFields" -> "ECMP Hashing"
    else -> propertyName.allCapitalized
}

fun Property.description(schema: Schema) =
"""
${propertyName.allCapitalized}
${schema.propertyDescription(parent, propertyName) ?: ""}
""".trim()

fun Property.conditionDescription(schema: Schema) =
"""
Define $title
${schema.propertyDescription(parent, propertyName) ?: ""}
""".trim()

val Property.actualListProperty get() =
    clazz.properties.properties[0]

private fun List<Property>.buildParameters(builder: ParameterAggregator, schema: Schema, config: Config, createMode: Boolean, propertyPath: () -> String) =
    forEach { it.toParameter(builder, schema, config, createMode, propertyPath) }

private fun Property.toParameter(builder: ParameterAggregator, schema: Schema, config: Config, createMode: Boolean, propertyPath: () -> String) = when {
    clazz.hasCustomInput -> customProperties[clazz]!!.run { this@toParameter.setup(builder, schema, createMode, propertyPath) }
    clazz.isStringListWrapper -> toStringListWrapperParameter(builder, schema, createMode, {
        FromComplexPropertyValue(item, "${propertyPath().preparePrefix()}$parameterName.${actualListProperty.parameterName}", string.array)
    })
    else -> toPrimitiveParameter(builder, schema, config, createMode, {
        FromComplexPropertyValue(item, "${propertyPath().preparePrefix()}$parameterName", clazz.parameterType)
    })
}

private fun Property.toPrimitiveParameter(builder: ParameterAggregator, schema: Schema, config: Config, createMode: Boolean, binding: () -> DataBinding<Any>) {
    builder.parameter(parameterName, clazz) {
        description = description(schema)
        if (!createMode)
            dataBinding = binding()
        val customValidationAction = config.customValidationAction(propertyName)
        if (customValidationAction == null)
            additionalQualifiers += schema.simpleTypeConstraints(parent, propertyName, ignoreMissing = true)
        else
            validWhen = validationActionCallTo(customValidationAction).asValidationCondition()
    }
}

private fun Property.toStringListWrapperParameter(builder: ParameterAggregator, schema: Schema, createMode: Boolean, binding: () -> DataBinding<Any>) {
    // This function should only be called for properties
    // that are just wrappers for lists of Strings
    val actualProperty = actualListProperty
    assert(actualProperty.clazz == String::class.java)
    builder.parameter(parameterName, string.array) {
        description = description(schema)
        if (!createMode)
            dataBinding = binding()
        additionalQualifiers += schema.simpleTypeConstraints(clazz, actualProperty.propertyName, ignoreMissing = true)
    }
}

private fun List<Property>.advancedSwitch(builder: ParameterAggregator, schema: Schema, propertyPath: String) {
    forEach {
        builder.parameter(it.parameterName.condition, boolean) {
            description = it.conditionDescription(schema)
            dataBinding = NullStateOfProperty(item, "${propertyPath.preparePrefix()}${it.parameterName}")
        }
    }
}

private fun workflowPropertiesFilter(property: Property, config: Config) : Boolean =
    property.declaringClass == property.parent &&
        ! property.clazz.isPropertyListWrapper &&
        ! config.ignoredInWorkflow(property.clazz) &&
        config.isEditableProperty(property.propertyName)

fun Class<*>.maxDepth(schema: Schema, config: Config, createMode: Boolean, level: Int): Int =
    properties.onlyComplex(schema, config, createMode, level)
        .map { it.clazz.maxDepth(schema, config, createMode, level + 1) + 1 }
        .max() ?: 0

fun Sequence<Property>.workflowProperties(schema: Schema, config: Config, createMode: Boolean, level: Int) =
    filter { workflowPropertiesFilter(it, config) }
    .filter { schema.propertyEditableInMode(it, createMode, level) }

val Property.isPrimitiveProperty get() =
    clazz.hasCustomInput || clazz.hasOnlyListOfStrings || ! clazz.isApiTypeClass

val Property.isComplexProperty get() =
    ! isPrimitiveProperty

fun Sequence<Property>.onlyPrimitives(schema: Schema, config: Config, createMode: Boolean, level: Int) =
    workflowProperties(schema, config, createMode, level)
    .filter { it.isPrimitiveProperty }

fun ClassProperties.onlyPrimitives(schema: Schema, config: Config, createMode: Boolean, level: Int) =
    simpleProperties.asSequence().onlyPrimitives(schema, config, createMode, level).toList()

fun Sequence<Property>.onlyComplex(schema: Schema, config: Config, createMode: Boolean, level: Int) =
    workflowProperties(schema, config, createMode, level)
    .filter { it.isComplexProperty }
    .filter { it.isNonEmptyComplexProperty(schema, config, createMode, level) }

fun Property.isNonEmptyComplexProperty(schema: Schema, config: Config, createMode: Boolean, level: Int): Boolean =
    isComplexProperty && clazz.properties.run {
        onlyPrimitives(schema, config, createMode, level + 1).isNotEmpty() ||
        onlyComplex(schema, config, createMode, level + 1).any { it.isNonEmptyComplexProperty(schema, config, createMode, level + 1) }
    }

fun ClassProperties.onlyComplex(schema: Schema, config: Config, createMode: Boolean, level: Int) =
    simpleProperties.asSequence().onlyComplex(schema, config, createMode, level).toList()

fun Class<*>.complexPropertiesInRange(range: IntRange, schema: Schema, config: Config, createMode: Boolean, level: Int) =
    properties.properties.asSequence()
        .filter { workflowPropertiesFilter(it, config) }
        .filter { ! it.isList }
        .filter { it.clazz.maxDepth(schema, config, createMode, level + 1) in range }

fun Class<*>.hasAnyEditableProperty(schema: Schema, config: Config) =
    ! properties.run {
        onlyPrimitives(schema, config, false, 0).isEmpty() &&
        onlyComplex(schema, config, false, 0).isEmpty() }

fun Schema.propertyEditableInMode(property: Property, createMode: Boolean, level: Int) =
    crudStatus(property.declaringClass, property.propertyName).run {
        if (createMode) isCreateOnly || level > 0 else isUpdate
    }

fun String.preparePrefix(): String = when {
    isBlank() -> this
    endsWith(".") -> this
    else -> "$this."
}

fun PresentationParametersBuilder.addProperties(
    clazz: Class<*>,
    schema: Schema,
    createMode: Boolean = false,
    propertyPrefix: String = "",
    extraVisibility: VisibilityCondition = AlwaysVisible,
    config: Config) {
    val properties = clazz.properties
    val topPrimitives = properties.onlyPrimitives(schema, config, createMode, 0)
    val topComplex = properties.onlyComplex(schema, config, createMode, 0)

    val propertyPath = propertyPrefix.preparePrefix()

    val basicVisibility = if (createMode) AlwaysVisible else WhenNonNull(item)
    val stepVisibility = basicVisibility and extraVisibility

    if (!topPrimitives.isEmpty()) {
        step(CustomParameters) {
            visibility = stepVisibility
            topPrimitives.buildParameters(this@step, schema, config, createMode) { propertyPath }
        }
    }

    if (!topComplex.isEmpty()) {
        step(AdvancedParameters) {
            visibility = stepVisibility
            topComplex.advancedSwitch(this@step, schema, propertyPath)
        }
    }

    for (prop in topComplex) {
        val propProperties = prop.clazz.properties
        val primitives = propProperties.onlyPrimitives(schema, config, createMode, 1)
        val complex = propProperties.onlyComplex(schema, config, createMode, 1)
            .filter { it.clazz.maxDepth(schema, config, createMode, 1) < 1 }
            .filter { it.clazz.properties.onlyPrimitives(schema, config, createMode, 1).isNotEmpty() }

        when {
            primitives.isEmpty() && !complex.isEmpty() -> groups(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                visibility = FromBooleanParameter(prop.parameterName.condition)
                group(AdvancedParameters) {
                    complex.advancedSwitch(this@group, schema, "$propertyPath${prop.parameterName}")
                }
                for (propProp in complex) {
                    group(propProp.title) {
                        description = schema.propertyDescription(propProp.parent, propProp.propertyName)
                        visibility = FromBooleanParameter(propProp.parameterName.condition)
                        propProp.clazz.properties.onlyPrimitives(schema, config, createMode, 2)
                            .buildParameters(this@group, schema, config, createMode) {
                                "$propertyPath${prop.parameterName}.${propProp.parameterName}"
                            }
                    }
                }
            }
            complex.isEmpty() && ! primitives.isEmpty() -> step(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                visibility = FromBooleanParameter(prop.parameterName.condition)
                primitives.buildParameters(this@step, schema, config, createMode) {
                    "$propertyPath${prop.parameterName}"
                }
            }
            else -> groups(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                visibility = FromBooleanParameter(prop.parameterName.condition)
                group(CustomParameters) {
                    primitives.buildParameters(this@group, schema, config, createMode) {
                        "$propertyPath${prop.parameterName}"
                    }
                }
                group(AdvancedParameters) {
                    complex.advancedSwitch(this@group, schema, "$propertyPath${prop.parameterName}")
                }
                for (propProp in complex) {
                    group(propProp.title) {
                        visibility = FromBooleanParameter(propProp.parameterName.condition)
                        description = schema.propertyDescription(propProp.parent, propProp.propertyName)
                        propProp.clazz.properties.onlyPrimitives(schema, config, createMode, 2)
                            .buildParameters(this@group, schema, config, createMode) {
                                "$propertyPath${prop.parameterName}.${propProp.parameterName}"
                            }
                    }
                }
            }
        }
    }
}

val List<ObjectClass>.addAllReferences get() =
    joinToString("\n") { it.addReferenceEntry }

private val Class<*>.addReferenceEntry get() =
"""
if($parameterName) {
    $item.add$pluginName($parameterName);
}
"""

val String.condition get() =
    "define${capitalize()}"

fun Class<*>.workflowEditableProperties(config: Config) =
    properties.simpleProperties
        .filter { workflowPropertiesFilter(it, config) }
