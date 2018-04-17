/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.allLowerCase
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isStringListWrapper
import net.juniper.contrail.vro.config.parameterName
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.generator.model.properties
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.dsl.FromBooleanParameter
import net.juniper.contrail.vro.workflows.dsl.NullStateOfProperty
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.DefaultValue
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.createWorkflowDescription
import net.juniper.contrail.vro.workflows.schema.propertyDescription
import net.juniper.contrail.vro.workflows.schema.relationDescription
import net.juniper.contrail.vro.workflows.schema.simpleTypeConstraints

fun createWorkflow(clazz: ObjectClass, parentClazz: ObjectClass?, multipleParents: Boolean, refs: List<ObjectClass>, schema: Schema): WorkflowDefinition {

    val workflowBaseName = "Create ${clazz.allLowerCase}"
    val workflowNameSuffix = if (parentClazz != null && multipleParents) " in ${parentClazz.allLowerCase}" else ""
    val workflowName = workflowBaseName + workflowNameSuffix
    val parentName = parentClazz?.pluginName ?: Connection

    return workflow(workflowName).withScript(clazz.createScriptBody(parentClazz, refs, schema)) {
        description = schema.createWorkflowDescription(clazz, parentClazz)
        parameter("name", string) {
            description = "${clazz.allCapitalized} name"
            mandatory = true

        }
        parameter(parent, parentName.reference) {
            description = "Parent ${parentName.allCapitalized}"
            mandatory = true

        }

        output(item, clazz.reference) {
            description = "${clazz.allCapitalized} created in this workflow"
        }

        for (ref in refs) {
            parameter(ref.parameterName, ref.reference) {
                description = schema.relationInCreateWorkflowDescription(clazz, ref)
                mandatory = true
            }
        }

        addProperties(
            clazz = clazz,
            schema = schema,
            createMode = true
        )
    }
}

fun editWorkflow(clazz: ObjectClass, schema: Schema): WorkflowDefinition {

    val workflowName = "Edit ${clazz.allLowerCase}"

    return workflow(workflowName).withScript(editScriptBody(clazz, schema)) {
        description = schema.createWorkflowDescription(clazz)
        parameter(item, clazz.reference) {
            description = "${clazz.allCapitalized} to edit"
            mandatory = true
            showInInventory = true
        }

        addProperties (
            clazz = clazz,
            schema = schema
        )
    }
}

fun editComplexPropertiesWorkflows(clazz: ObjectClass, schema: Schema) =
    clazz.complexPropertiesInRange(2..3, schema, false, 0)
        .map { it.complexEditWorkflows(schema, 0) }
        .flatten()
        .toList()

private fun Property.complexEditWorkflows(schema: Schema, level: Int) =
    clazz.complexPropertiesInRange(1..2, schema, false, level)
        .map { editComplexPropertyWorkflows(this, it, schema) }

private fun editComplexPropertyWorkflows(rootProperty: Property, thisProperty: Property, schema: Schema): WorkflowDefinition {

    val rootClass = rootProperty.parent
    val workflowName = "Edit ${thisProperty.clazz.allLowerCase} of ${rootClass.allLowerCase}"

    return workflow(workflowName).withScript(editComplexPropertyScriptBody(schema, rootProperty, thisProperty)) {
        description = schema.propertyDescription(rootProperty.clazz, thisProperty.propertyName)
        parameter(item, rootClass.reference) {
            description = "${rootClass.allCapitalized} to edit"
            mandatory = true
            showInInventory = true
        }
        parameter(thisProperty.propertyName.condition, boolean) {
            description = "Define ${thisProperty.title}"
            dataBinding = NullStateOfProperty(item, "${rootProperty.propertyName}.${thisProperty.propertyName}")
            visibility = WhenNonNull(item)
        }

        addProperties (
            clazz = thisProperty.clazz,
            schema = schema,
            propertyPrefix = "${rootProperty.propertyName}.${thisProperty.propertyName}",
            extraVisibility = FromBooleanParameter(thisProperty.propertyName.condition)
        )
    }
}

fun deleteWorkflow(clazz: ObjectClass) =
    deleteWorkflow(clazz.pluginName, deleteScriptBody())

private fun Schema.relationInCreateWorkflowDescription(parentClazz: ObjectClass, clazz: ObjectClass) = """
${clazz.allCapitalized}
${relationDescription(parentClazz, clazz)}
""".trim()

private fun setParentCall(parentClazz: ObjectClass?) =
    if (parentClazz == null)
        "$item.setParent$Connection($parent);"
    else
        "$item.setParent${parentClazz.pluginName}($parent);"

private fun Class<*>.setBooleanDefaults(schema: Schema) =
    properties.simpleProperties.asSequence()
        .filter { it.clazz == java.lang.Boolean.TYPE }
        .filter { it.isTrueByDefault(schema) }
        .joinToString("\n") { "$item.${it.propertyName} = true;" }

private fun Property.isTrueByDefault(schema: Schema) =
    schema.simpleTypeConstraints(parent, propertyName)
        .filterIsInstance(DefaultValue::class.java)
        .filter { it.value == true }
        .any()

private fun Class<*>.createScriptBody(parentClazz: ObjectClass?, references: List<ObjectClass>, schema: Schema) = """
$item = new Contrail$pluginName();
$item.setName(name);
${references.addAllReferences}
${setParentCall(parentClazz)}
${setBooleanDefaults(schema)}
${editPropertiesCode(item, schema, createMode = true)}
$item.create();
""".trimIndent()

private fun editScriptBody(clazz: Class<*>, schema: Schema) = """
${clazz.editPropertiesCode(item, schema, createMode = false)}
$item.update();
""".trimIndent()

private fun editComplexPropertyScriptBody(schema: Schema, rootProperty: Property, thisProperty: Property) = """
${initComplexPropertyEdit(schema, rootProperty.propertyName, rootProperty.clazz, thisProperty.propertyName, thisProperty.clazz)}
$item.update();
""".trimIndent()

private fun initComplexPropertyEdit(schema: Schema, rootName: String, rootClass: Class<*>, thisName: String, thisClass: Class<*>) = """
var $rootName = $item.get${rootName.capitalize()}();
if (${thisName.condition}) {
    var $thisName = null;
    if (!$rootName) {
        $rootName = new Contrail${rootClass.pluginName}();
        $item.set${rootName.capitalize()}($rootName);
    } else {
        $thisName = $rootName.get${thisName.capitalize()}();
    }
    if (!$thisName) {
        $thisName = new Contrail${thisClass.pluginName}();
        $rootName.set${thisName.capitalize()}($thisName);
    }
${thisClass.editPropertiesCode(thisName, schema, false).prependIndent(tab)}
} else {
    if ($rootName) {
        $rootName.set${thisName.capitalize()}(null);
    }
}
""".trim()

private fun deleteScriptBody() = """
$item.delete();
""".trimIndent()

fun Class<*>.editPropertiesCode(item: String, schema: Schema, createMode: Boolean, level: Int = 0) =
    workflowEditableProperties.asSequence().map { it.editCode(item, schema, createMode, level) }
        .filter { !it.isBlank() }.joinToString("\n")

fun Property.editCode(item: String, schema: Schema, createMode: Boolean, level: Int) = when {
    ! schema.propertyEditableInMode(this, createMode, level) -> ""
    (clazz.hasCustomInput || ! clazz.isApiTypeClass) && level <= maxPrimitiveLevel -> primitiveEditCode(item)
    clazz.isStringListWrapper && level <= maxPrimitiveLevel -> listEditCode(item)
    clazz.isApiTypeClass && !clazz.hasCustomInput &&
        (level + clazz.maxDepth(schema, createMode, level) <= maxComplexLevel || level == 0) -> complexEditCode(item, schema, createMode, level)
    else -> ""
}

private val Property.propertyValue get() =
    customProperties[clazz]?.code(propertyName) ?: propertyName

fun Property.primitiveEditCode(item: String) =
    "$item.set${propertyName.capitalize()}($propertyValue);"

fun Property.listEditCode(item: String) =
    "$item.set${propertyName.capitalize()}(new Contrail${clazz.pluginName}($propertyName));"

fun Property.complexEditCode(item: String, schema: Schema, createMode: Boolean, level: Int): String = """
var $propertyName = $item.get${propertyName.capitalize()}();
if (${propertyName.condition}) {
    if (!$propertyName) $propertyName = new Contrail${clazz.pluginName}();
${clazz.editPropertiesCode(propertyName, schema, createMode, level + 1).prependIndent(tab)}
} else {
    $propertyName = null;
}
$item.set${propertyName.capitalize()}($propertyName);
""".trim()