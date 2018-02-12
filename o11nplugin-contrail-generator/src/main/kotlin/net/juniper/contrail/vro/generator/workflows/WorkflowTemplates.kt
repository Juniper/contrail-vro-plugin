/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.camelChunks
import net.juniper.contrail.vro.config.ignoredInWorkflow
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.generator.model.properties
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isEditableProperty
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.hasOnlyListOfStrings
import net.juniper.contrail.vro.config.isPropertyListWrapper
import net.juniper.contrail.vro.config.isStringListWrapper
import net.juniper.contrail.vro.config.parameterName
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.splitCamel
import net.juniper.contrail.vro.generator.Contrail
import net.juniper.contrail.vro.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.AlwaysVisible
import net.juniper.contrail.vro.workflows.model.DataBinding
import net.juniper.contrail.vro.workflows.model.Element
import net.juniper.contrail.vro.workflows.model.FromBooleanParameter
import net.juniper.contrail.vro.workflows.model.FromComplexPropertyValue
import net.juniper.contrail.vro.workflows.model.FromSimplePropertyValue
import net.juniper.contrail.vro.workflows.model.NullStateOfProperty
import net.juniper.contrail.vro.workflows.model.WhenNonNull
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.createDunesProperties
import net.juniper.contrail.vro.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.workflows.model.parameterType
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.simpleTypeQualifiers

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

val attribute = "attribute"
val executor = "executor"
val tab = "    "
val AdvancedParameters = "Advanced parameters"
val CustomParameters = "Custom parameters"

val String.allLowerCase get() =
    splitCamel().toLowerCase()

val Class<*>.allLowerCase get() =
    pluginName.allLowerCase

val String.allCapitalized get() =
    replace(typeSuffix, "").camelChunks.joinToString(" ") { it.capitalize() }

val Class<*>.allCapitalized get() =
    pluginName.allCapitalized

val typeSuffix = "Type$".toRegex()

fun deleteWorkflow(className: String, scriptBody: String) =
    workflow("Delete ${className.allLowerCase}").withScript(scriptBody) {
        parameter(item, className.reference) {
            description = "${className.allCapitalized} to delete"
            mandatory = true
            showInInventory = true
        }
    }

private val Property.title get() = when (propertyName) {
    "mgmt" -> "Configuration"
    "ecmpHashingIncludeFields" -> "ECMP Hashing"
    else -> propertyName.allCapitalized
}

private fun Property.description(schema: Schema) =
"""
${propertyName.allCapitalized}
${schema.propertyDescription(parent, propertyName) ?: ""}
""".trim()

private fun Property.conditionDescription(schema: Schema) =
"""
Define $title
${schema.propertyDescription(parent, propertyName) ?: ""}
""".trim()

val Class<*>.maxDepth: Int get() =
    properties.simpleProperties.onlyComplex
        .map { it.clazz.maxDepth + 1 }
        .max() ?: 0

val Property.actualListProperty get() =
    clazz.properties.properties[0]

typealias PropertyBinding = (Property) -> DataBinding<Any>

private val Property.simpleBinding: PropertyBinding get() =
    if (clazz.isStringListWrapper) {
        { FromComplexPropertyValue(item, "${it.propertyName}.${it.actualListProperty.propertyName}", string.array) }
    } else {
        { FromSimplePropertyValue(item, it.propertyName, it.clazz.parameterType) }
    }

private val Property.childBinding: PropertyBinding get() =
    if (clazz.isStringListWrapper) {
        { FromComplexPropertyValue(item, "$propertyName.${it.propertyName}.${it.actualListProperty.propertyName}", string.array) }
    } else {
        { FromComplexPropertyValue(item, "$propertyName.${it.propertyName}", it.clazz.parameterType) }
    }

private fun Property.grandChildBinding(child: Property): PropertyBinding =
    if (clazz.isStringListWrapper) {
        { FromComplexPropertyValue(item, "$propertyName.${child.propertyName}.${it.propertyName}.${it.actualListProperty.propertyName}", string.array) }
    } else {
        { FromComplexPropertyValue(item, "$propertyName.${child.propertyName}.${it.propertyName}", it.clazz.parameterType) }
    }

private fun Property.toParameter(builder: ParameterAggregator, schema: Schema, editMode: Boolean) =
    toParameter(builder, schema, editMode, simpleBinding)

private fun Property.toParameter(builder: ParameterAggregator, schema: Schema, editMode: Boolean, binding: PropertyBinding) =
    if (clazz.isStringListWrapper)
        toStringListWrapperParameter(builder, schema, editMode, binding)
    else
        toPrimitiveParameter(builder, schema, editMode, binding)

private fun Property.toPrimitiveParameter(builder: ParameterAggregator, schema: Schema, editMode: Boolean, binding: PropertyBinding) {
    builder.parameter(propertyName, clazz) {
        description = description(schema)
        if (editMode)
            dataBinding = binding(this@toPrimitiveParameter)
        additionalQualifiers += schema.simpleTypeQualifiers(parent, propertyName)
    }
}

private fun Property.toStringListWrapperParameter(builder: ParameterAggregator, schema: Schema, editMode: Boolean, binding: PropertyBinding) {
    // This function should only be called for properties
    // that are just wrappers for lists of Strings
    val actualProperty = actualListProperty
    assert(actualProperty.clazz == String::class.java)
    builder.parameter(propertyName, string.array) {
        description = description(schema)
        if (editMode)
            dataBinding = binding(this@toStringListWrapperParameter)
        additionalQualifiers += schema.simpleTypeQualifiers(clazz, actualProperty.propertyName)
    }
}

private fun List<Property>.advancedSwitch(builder: ParameterAggregator, schema: Schema, propertyPrefix: String, editMode: Boolean) {
    forEach {
        builder.parameter(it.propertyName.condition, boolean) {
            description = it.conditionDescription(schema)
            if (editMode)
                dataBinding = NullStateOfProperty(item, "$propertyPrefix${it.propertyName}")
        }
    }
}

private val workflowPropertiesFilter: (Property) -> Boolean =
    { it.declaringClass == it.parent &&
        ! it.clazz.isPropertyListWrapper &&
        ! it.clazz.ignoredInWorkflow &&
        it.propertyName.isEditableProperty }

private val Sequence<Property>.onlyPrimitives get() =
    filter(workflowPropertiesFilter)
    .filter { it.clazz.hasOnlyListOfStrings || ! it.clazz.isApiTypeClass }

private val List<Property>.onlyPrimitives get() =
    asSequence().onlyPrimitives.toList()

private val Sequence<Property>.onlyComplex get() =
    filter(workflowPropertiesFilter)
    .filter { it.clazz.isApiTypeClass && ! it.clazz.hasOnlyListOfStrings }

private val List<Property>.onlyComplex get() =
    asSequence().onlyComplex.toList()

fun PresentationParametersBuilder.addProperties(clazz: Class<*>, schema: Schema, editMode: Boolean = false) {
    val properties = clazz.properties
    val topPrimitives = properties.simpleProperties.onlyPrimitives
    val topComplex = properties.simpleProperties.onlyComplex.filter { it.clazz.maxDepth < 2 }

    val stepVisibility = if (editMode) WhenNonNull(item) else AlwaysVisible

    if (!topPrimitives.isEmpty()) {
        step(CustomParameters) {
            visibility = stepVisibility
            topPrimitives.forEach { it.toParameter(this@step, schema, editMode) }
        }
    }

    if (!topComplex.isEmpty()) {
        step(AdvancedParameters) {
            visibility = stepVisibility
            topComplex.advancedSwitch(this@step, schema, "", editMode)
        }
    }

    for (prop in topComplex) {
        val propProperties = prop.clazz.properties
        val primitives = propProperties.simpleProperties.onlyPrimitives
        val complex = propProperties.simpleProperties.onlyComplex.filter { it.clazz.maxDepth < 1 }

        when {
            primitives.isEmpty() && !complex.isEmpty() -> groups(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                visibility = FromBooleanParameter(prop.propertyName.condition)
                group(AdvancedParameters) {
                    complex.advancedSwitch(this@group, schema, "${prop.propertyName}.", editMode)
                }
                for (propProp in complex) {
                    group(propProp.title) {
                        description = schema.propertyDescription(propProp.parent, propProp.propertyName)
                        visibility = FromBooleanParameter(propProp.propertyName.condition)
                        propProp.clazz.properties.simpleProperties.forEach {
                            it.toParameter(this@group, schema, editMode, prop.grandChildBinding(propProp))
                        }
                    }
                }
            }
            complex.isEmpty() && ! primitives.isEmpty() -> step(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                visibility = FromBooleanParameter(prop.propertyName.condition)
                primitives.forEach { it.toParameter(this@step, schema, editMode, prop.childBinding) }
            }
            else -> groups(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                visibility = FromBooleanParameter(prop.propertyName.condition)
                group(CustomParameters) {
                    primitives.forEach { it.toParameter(this@group, schema, editMode, prop.childBinding) }
                }
                group(AdvancedParameters) {
                    complex.advancedSwitch(this@group, schema, "${prop.propertyName}.", editMode)
                }
                for (propProp in complex) {
                    group(propProp.title) {
                        visibility = FromBooleanParameter(propProp.propertyName.condition)
                        description = schema.propertyDescription(propProp.parent, propProp.propertyName)
                        propProp.clazz.properties.simpleProperties.onlyPrimitives.forEach {
                            it.toParameter(this@group, schema, editMode, prop.grandChildBinding(propProp))
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

private fun Property.setCode(ref: String) =
    "$ref.set${propertyName.capitalize()}($propertyName);"

val String.condition get() =
    "define_$this"

fun Property.propertyCode(prefix: String): String =
    if (clazz.isApiTypeClass) clazz.attributeCode(propertyName, prefix, true) else ""

fun Class<*>.attributeCode(ref: String, prefix: String = "", init: Boolean = false) =
    workflowEditableProperties.run {
if (init)
"""
${prepare(prefix)}
var $ref = null;
if (${ref.condition}) {
    $ref = new Contrail$pluginName();
    ${assign(ref, prefix+tab)}
}
"""
else
"""
${prepare(prefix)}
${assign(ref, prefix)}
"""
}.trimStart().prependIndent(prefix)

val Class<*>.workflowEditableProperties get() =
    properties.simpleProperties
        .filter(workflowPropertiesFilter)

fun List<Property>.assign(ref: String, prefix: String = tab) =
    joinToString(separator = "\n$prefix") { it.setCode(ref) }

fun List<Property>.prepare(prefix: String) =
    joinToString(separator = "") { it.propertyCode(prefix) }

val String.retrieveExecutor get() =
    "var $executor = ContrailConnectionManager.executor($this.internalId.toString());"

val retrieveExecutorFromItem get() =
    item.retrieveExecutor

fun String.updateAsClass(className: String) =
    "$executor.update$className($this);"

fun String.deleteAsClass(className: String) =
    "$executor.delete$className($this);"

val ForwardRelation.updateParent get() =
    parent.updateAsClass(parentClass.pluginName)

val ForwardRelation.updateItem get() =
    item.updateAsClass(parentClass.pluginName)

val ForwardRelation.retrieveExecutorAndUpdateItem get() =
"""
$retrieveExecutorFromItem
$updateItem
""".trimIndent()