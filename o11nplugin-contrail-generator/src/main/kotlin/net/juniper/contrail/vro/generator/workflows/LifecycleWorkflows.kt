/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.allLowerCase
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.isConfigRoot
import net.juniper.contrail.vro.config.isDefaultRoot
import net.juniper.contrail.vro.config.isPolicyManagement
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.toPluginMethodName
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.config.defaultParentType
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isStringListWrapper
import net.juniper.contrail.vro.config.objectType
import net.juniper.contrail.vro.config.parameterName
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.createWorkflowDescription
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.relationDescription
import net.juniper.contrail.vro.workflows.dsl.FromBooleanParameter
import net.juniper.contrail.vro.workflows.dsl.NullStateOfProperty
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.createWorkflowName
import net.juniper.contrail.vro.workflows.util.editWorkflowName

fun createWorkflows(clazz: ObjectClass, refs: List<ObjectClass>, schema: Schema, config: Config): List<WorkflowDefinition> {
    val parentsInModel = config.numberOfParentsInModel(clazz)
    val rootParents = config.hasRootParent(clazz) && !config.isHiddenRoot(clazz)

    return config.parents(clazz).filter { config.isModelClass(it) || it.isDefaultRoot }
        .map { createWorkflow(clazz, it, parentsInModel, rootParents, refs, schema, config) }
        .toList()
}

fun createWorkflow(clazz: ObjectClass, parentClazz: ObjectClass, parentsInModel: Int, hasRootParents: Boolean, refs: List<ObjectClass>, schema: Schema, config: Config): WorkflowDefinition {
    val workflowName = createWorkflowName(clazz, parentClazz, parentsInModel, hasRootParents)
    val parentName = if (config.isModelClass(parentClazz) && ! parentClazz.isPolicyManagement)
        parentClazz.pluginName
    else
        Connection

    return workflow(workflowName).withScript(clazz.createScriptBody(parentClazz, refs, schema, config)) {
        description = createWorkflowDescription(schema, clazz, config, parentClazz)
        parameter("name", string) {
            description = "${clazz.allCapitalized} name"
            mandatory = true

        }
        parameter(parent, parentName.reference) {
            description = "Parent ${parentName.allCapitalized}"
            mandatory = true
            if (parentName == Connection)
                dataBinding = fromAction(defaultConnection, type) {}
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
            createMode = true,
            config = config
        )
    }
}

fun editWorkflow(clazz: ObjectClass, schema: Schema, config: Config): WorkflowDefinition? {
    if (! clazz.hasAnyEditableProperty(schema, config)) return null

    val workflowName = editWorkflowName(clazz)

    return workflow(workflowName).withScript(editScriptBody(clazz, schema, config)) {
        description = createWorkflowDescription(schema, clazz, config)
        parameter(item, clazz.reference) {
            description = "${clazz.allCapitalized} to edit"
            mandatory = true
            showInInventory = true
        }

        addProperties (
            clazz = clazz,
            schema = schema,
            config = config
        )
    }
}

fun editComplexPropertiesWorkflows(clazz: ObjectClass, schema: Schema, config: Config) =
    clazz.complexPropertiesInRange(2..3, schema, config, false, 0)
        .map { it.complexEditWorkflows(schema, config, 0) }
        .flatten()
        .toList()

private fun Property.complexEditWorkflows(schema: Schema, config: Config, level: Int) =
    clazz.complexPropertiesInRange(1..2, schema, config, false, level)
        .map { editComplexPropertyWorkflows(this, it, schema, config) }

private fun editComplexPropertyWorkflows(rootProperty: Property, thisProperty: Property, schema: Schema, config: Config): WorkflowDefinition {

    val rootClass = rootProperty.parent
    val workflowName = "Edit ${thisProperty.clazz.allLowerCase} of ${rootClass.allLowerCase}"

    return workflow(workflowName).withScript(editComplexPropertyScriptBody(schema, config, rootProperty, thisProperty)) {
        description = schema.propertyDescription(rootProperty.clazz, thisProperty.parameterName)
        parameter(item, rootClass.reference) {
            description = "${rootClass.allCapitalized} to edit"
            mandatory = true
            showInInventory = true
        }
        parameter(thisProperty.parameterName.condition, boolean) {
            description = "Define ${thisProperty.title}"
            dataBinding = NullStateOfProperty(item, "${rootProperty.parameterName}.${thisProperty.parameterName}")
            visibility = WhenNonNull(item)
        }

        addProperties (
            clazz = thisProperty.clazz,
            schema = schema,
            propertyPrefix = "${rootProperty.parameterName}.${thisProperty.parameterName}",
            extraVisibility = FromBooleanParameter(thisProperty.parameterName.condition),
            config = config
        )
    }
}

fun deleteWorkflow(clazz: ObjectClass) =
    deleteWorkflow(clazz.pluginName, deleteScriptBody())

private fun Schema.relationInCreateWorkflowDescription(parentClazz: ObjectClass, clazz: ObjectClass) = """
${clazz.allCapitalized}
${relationDescription(parentClazz, clazz)}
""".trim()

private fun ObjectClass.setParentCall(parentClazz: ObjectClass, config: Config) = when {
    parentClazz.isPolicyManagement -> setParentPolicyManagementCall()
    config.isModelClass(parentClazz) -> setRegularParentCall(parentClazz)
    else -> setRootParentCall(parentClazz) + "\n" + setParentConnectionCall()
}

private fun setParentConnectionCall() =
    "$item.setParent$Connection($parent);"

private fun setRegularParentCall(parentClazz: ObjectClass) =
    "$item.setParent${parentClazz.pluginName}($parent);"

private fun setParentPolicyManagementCall() = """
var defaultPolicyManagement = parent.findPolicyManagementByFQName("default-policy-management");
$item.setParentPolicyManagement(defaultPolicyManagement);
""".trim()

private fun ObjectClass.setRootParentCall(parentClazz: ObjectClass) = when {
    // create empty ConfigRoot object just to configure parent type
    parentClazz.isConfigRoot -> "$item.setParent${parentClazz.pluginName}(new Contrail${parentClazz.pluginName}());"
    // default parent will be set by the API
    defaultParentType == parentClazz.objectType -> ""
    else -> throw IllegalArgumentException("Unable to create parent ${parentClazz.simpleName} for $simpleName.")
}

private fun ObjectClass.createScriptBody(parentClazz: ObjectClass, references: List<ObjectClass>, schema: Schema, config: Config) = """
$item = new Contrail$pluginName();
$item.setName(name);
${references.addAllReferences}
${setParentCall(parentClazz, config)}
${editPropertiesCode(item, schema, config, createMode = true)}
$item.create();
""".trimIndent().lineSequence().filter { it.isNotBlank() }.joinToString("\n")

private fun editScriptBody(clazz: Class<*>, schema: Schema, config: Config) = """
${clazz.editPropertiesCode(item, schema, config, createMode = false)}
$item.update();
""".trimIndent()

private fun editComplexPropertyScriptBody(schema: Schema, config: Config, rootProperty: Property, thisProperty: Property) = """
${initComplexPropertyEdit(schema, config, rootProperty.parameterName, rootProperty.clazz, thisProperty.parameterName, thisProperty.clazz)}
$item.update();
""".trimIndent()

private fun initComplexPropertyEdit(schema: Schema, config: Config, rootName: String, rootClass: Class<*>, thisName: String, thisClass: Class<*>) = """
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
${thisClass.editPropertiesCode(thisName, schema, config, false).prependIndent(tab)}
} else {
    if ($rootName) {
        $rootName.set${thisName.capitalize()}(null);
    }
}
""".trim()

private fun deleteScriptBody() = """
$item.delete();
""".trimIndent()

fun Class<*>.editPropertiesCode(item: String, schema: Schema, config: Config, createMode: Boolean, level: Int = 0) =
    workflowEditableProperties(config).asSequence().map { it.editCode(item, schema, config, createMode, level) }
        .filter { !it.isBlank() }.joinToString("\n")

fun Property.editCode(item: String, schema: Schema, config: Config, createMode: Boolean, level: Int) = when {
    ! schema.propertyEditableInMode(this, createMode, level) -> ""
    (clazz.hasCustomInput || ! clazz.isApiTypeClass) && level <= maxPrimitiveLevel -> primitiveEditCode(item)
    clazz.isStringListWrapper && level <= maxPrimitiveLevel -> listEditCode(item)
    clazz.isApiTypeClass && !clazz.hasCustomInput &&
        (level + clazz.maxDepth(schema, config, createMode, level) <= maxComplexLevel || level == 0) -> complexEditCode(item, schema, config, createMode, level)
    else -> ""
}

private val Property.propertyValue get() =
    customProperties[clazz]?.code(parameterName) ?: parameterName

private val String.pluginMethodName get() =
    capitalize().toPluginMethodName

private val Property.pluginMethodName get() =
    pluginPropertyName.capitalize()

fun Property.primitiveEditCode(item: String) =
    "$item.set$pluginMethodName($propertyValue);"

fun Property.listEditCode(item: String) =
    "$item.set$pluginMethodName(new Contrail${clazz.pluginName}($parameterName));"

fun Property.complexEditCode(item: String, schema: Schema, config: Config, createMode: Boolean, level: Int): String = """
var $parameterName = $item.get$pluginMethodName();
if (${parameterName.condition}) {
    if (!$parameterName) $parameterName = new Contrail${clazz.pluginName}();
${clazz.editPropertiesCode(parameterName, schema, config, createMode, level + 1).prependIndent(tab)}
} else {
    $parameterName = null;
}
$item.set$pluginMethodName($parameterName);
""".trim()