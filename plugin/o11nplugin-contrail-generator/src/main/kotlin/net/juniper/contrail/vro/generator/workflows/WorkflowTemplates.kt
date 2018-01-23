/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.bold
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.config.ignoredInWorkflow
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.generator.model.properties
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isEditableProperty
import net.juniper.contrail.vro.config.isPropertyOrStringListWrapper
import net.juniper.contrail.vro.config.pluralParameterName
import net.juniper.contrail.vro.config.splitCamel
import net.juniper.contrail.vro.generator.workflows.dsl.ParameterAggregator
import net.juniper.contrail.vro.generator.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.generator.workflows.model.SecureString
import net.juniper.contrail.vro.generator.workflows.model.Workflow
import net.juniper.contrail.vro.generator.workflows.dsl.andParameters
import net.juniper.contrail.vro.generator.workflows.dsl.packagedIn
import net.juniper.contrail.vro.generator.workflows.dsl.withScript
import net.juniper.contrail.vro.generator.workflows.dsl.withVersion
import net.juniper.contrail.vro.generator.workflows.model.Action
import net.juniper.contrail.vro.generator.workflows.model.Element
import net.juniper.contrail.vro.generator.workflows.model.Reference
import net.juniper.contrail.vro.generator.workflows.model.WhenNonNull
import net.juniper.contrail.vro.generator.workflows.model.array
import net.juniper.contrail.vro.generator.workflows.model.createDunesProperties
import net.juniper.contrail.vro.generator.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string
import net.juniper.contrail.vro.generator.workflows.xsd.Schema
import net.juniper.contrail.vro.generator.workflows.xsd.objectDescription
import net.juniper.contrail.vro.generator.workflows.xsd.propertyDescription
import net.juniper.contrail.vro.generator.workflows.xsd.relationDescription

fun Element.elementInfoPropertiesFor(categoryPath: String) = createElementInfoProperties(
    categoryPath = categoryPath,
    type = elementType,
    name = outputName,
    id = id
)

fun dunesPropertiesFor(info: ProjectInfo) = createDunesProperties(
    pkgDescription = "Contrail package",
    pkgName = info.workflowsPackageName,
    usedPlugins = "Contrail#${info.baseVersion}",
    pkgOwner = "Juniper",
    pkgId = "4452345677834623546675023032605023032"
)

val Connection = "Connection"
val parent = "parent"
val child = "child"
val item = "item"
val attribute = "attribute"
val executor = "executor"
val tab = "    "

private val String.workflowNameFormat get() =
    splitCamel().toLowerCase()

private val Class<*>.workflowNameFormat get() =
    simpleName.workflowNameFormat

private val String.descriptionFormat get() =
    replace(typeSuffix, "").splitCamel()

private val Class<*>.descriptionFormat get() =
    simpleName.descriptionFormat

private val typeSuffix = "Type$".toRegex()

val ProjectInfo.workflowVersion get() =
    "$baseVersion.$buildNumber"

fun ProjectInfo.versionOf(name: String) =
    name packagedIn workflowsPackageName withVersion workflowVersion

fun createConnectionWorkflow(info: ProjectInfo): Workflow {

    val workflowName = "Create Contrail connection"

    return info.versionOf(workflowName) withScript createConnectionScriptBody andParameters {
        step("Controller") {
            parameter("name", string) {
                description = "Connection name"
                mandatory = true
                defaultValue = "Controller"
            }
            parameter("host", string) {
                description = "Contrail host"
                mandatory = true
            }
            parameter("port", number) {
                description = "Contrail port"
                mandatory = true
                defaultValue = 8082
                min = 0
                max = 65535
            }
        }
        step("Credentials") {
            parameter("username", string) {
                description = "User name"
            }
            parameter("password", SecureString) {
                description = "User password"
            }
            parameter("authServer", string) {
                description = "Authentication server"
            }
        }
        step("Tenant") {
            parameter("tenant", string) {
                description = "Tenant"
            }
        }
    }
}

private fun Schema.createWorkflowDescription(clazz: ObjectClass) : String? {
    val objectDescription = objectDescription(clazz) ?: return null
    return """
        ${clazz.descriptionFormat.bold}
        $objectDescription
    """.trimIndent()
}

private fun Schema.relationInCreateWorkflowDescription(parentClazz: ObjectClass, clazz: ObjectClass) : String {
    val relationDescription = relationDescription(parentClazz, clazz)
    return """
        ${clazz.folderName.bold}
        $relationDescription
    """.trimIndent()
}

fun createWorkflow(info: ProjectInfo, clazz: ObjectClass, parentClazz: ObjectClass?, refs: List<ObjectClass>, schema: Schema): Workflow {

    val workflowName = "Create ${clazz.workflowNameFormat}"
    val parentName = parentClazz?.simpleName ?: Connection

    return info.versionOf(workflowName) withScript createScriptBody(clazz, parentClazz, refs) andParameters {
        description = schema.createWorkflowDescription(clazz)
        parameter("name", string) {
            description = "${clazz.descriptionFormat} name"
            mandatory = true

        }
        parameter(parent, parentName.reference) {
            description = "Parent ${parentName.descriptionFormat}"
            mandatory = true

        }

        if (!refs.isEmpty()) {
            step("References") {
                for (ref in refs) {
                    parameter(ref.pluralParameterName, Reference(ref).array) {
                        description = schema.relationInCreateWorkflowDescription(clazz, ref)
                    }
                }
            }
        }

        addProperties(clazz, schema)
    }
}

fun deleteConnectionWorkflow(info: ProjectInfo): Workflow =
    deleteWorkflow(info, Connection, deleteConnectionScriptBody)

fun deleteWorkflow(info: ProjectInfo, clazz: ObjectClass): Workflow =
    deleteWorkflow(info, clazz.simpleName, deleteScriptBody(clazz.simpleName))

fun deleteWorkflow(info: ProjectInfo, className: String, scriptBody: String): Workflow {

    val workflowName = "Delete ${className.workflowNameFormat}"

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter(item, className.reference) {
            description = "${className.descriptionFormat} to delete"
            mandatory = true
            showInInventory = true
        }
    }
}

private val Property.title get() =
    propertyName.descriptionFormat.capitalize()

private fun Property.description(schema: Schema) =
    """
    ${propertyName.splitCamel().capitalize()}
    ${schema.propertyDescription(parent, propertyName) ?: ""}
    """.trimIndent()

private fun Property.toParameter(builder: ParameterAggregator, schema: Schema) {
    builder.parameter(propertyName, clazz) {
        description = description(schema)
    }
}

val workflowPropertiesFilter: (Property) -> Boolean =
    { it.declaringClass == it.parent &&
        ! it.clazz.isPropertyOrStringListWrapper &&
        ! it.clazz.ignoredInWorkflow &&
        it.propertyName.isEditableProperty }

val List<Property>.onlyPrimitives get() =
    filter { workflowPropertiesFilter(it) && ! it.clazz.isApiTypeClass }

val List<Property>.onlyComplex get() =
    filter { workflowPropertiesFilter(it) && it.clazz.isApiTypeClass }

private fun PresentationParametersBuilder.addProperties(clazz: Class<*>, schema: Schema) {
    val properties = clazz.properties
    val topPrimitives = properties.simpleProperties.onlyPrimitives
    val topComplex = properties.simpleProperties.onlyComplex

    if (!topPrimitives.isEmpty()) {
        step("Custom Parameters") {
            topPrimitives.forEach { it.toParameter(this@step, schema) }
        }
    }

    for (prop in topComplex) {
        val propProperties = prop.clazz.properties
        val primitives = propProperties.simpleProperties.onlyPrimitives
        val complex = propProperties.simpleProperties.onlyComplex

        when {
            primitives.isEmpty() -> groups(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                for (propProp in complex) {
                    group(propProp.title) {
                        description = schema.propertyDescription(propProp.parent, propProp.propertyName)
                        propProp.clazz.properties.simpleProperties.forEach { it.toParameter(this@group, schema) }
                    }
                }
            }
            complex.isEmpty() -> step(prop.title) {
                description = schema.propertyDescription(clazz, prop.propertyName)
                primitives.forEach { it.toParameter(this@step, schema) }
            }
            else -> Unit // TODO("Add support for attributes with mixed structure.")
        }
    }
}

private fun Schema.descriptionInCreateRelationWorkflow(parentClazz: ObjectClass, clazz: ObjectClass) : String {
    val relationDescription = relationDescription(parentClazz, clazz)
    return """
        ${clazz.descriptionFormat.capitalize()} to be added.
        $relationDescription
    """.trimIndent()
}

fun addReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation, schema: Schema): Workflow {

    val parentName = relation.parentName
    val childName = relation.childName
    val workflowName = "Add ${childName.workflowNameFormat} to ${parentName.workflowNameFormat}"
    val scriptBody = relation.addReferenceRelationScriptBody()

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter(parent, parentName.reference) {
            description = "${parentName.descriptionFormat.capitalize()} to add to"
            mandatory = true
        }
        parameter(child, childName.reference) {
            description = schema.descriptionInCreateRelationWorkflow(relation.parentClass, relation.childClass)
            mandatory = true
        }
        if ( ! relation.simpleReference) {
            addProperties(relation.attribute, schema)
        }
    }
}

fun removeReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation, action: Action): Workflow {

    val parentName = relation.parentName
    val childName = relation.childName
    val workflowName = "Remove ${childName.workflowNameFormat} from ${parentName.workflowNameFormat}"
    val scriptBody = relation.removeReferenceRelationScriptBody()

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter(parent, parentName.reference) {
            description = "${parentName.descriptionFormat.capitalize()} to remove from"
            mandatory = true
        }
        parameter(child, childName.reference) {
            description = "${childName.descriptionFormat.capitalize()} to be removed"
            mandatory = true
            visibility = WhenNonNull(parent)
            listedBy(action)
        }
    }
}

private val createConnectionScriptBody = """
var connectionId = ContrailConnectionManager.create(name, host, port, username, password, authServer, tenant);
System.log("Created connection with ID: " + connectionId);
""".trimIndent()

private val deleteConnectionScriptBody = """
ContrailConnectionManager.delete($item);
""".trimIndent()

private fun createScriptBody(clazz: Class<*>, parentClazz: ObjectClass?, references: List<ObjectClass>) = """
${parent.retrieveExecutor}
var $item = new Contrail${clazz.simpleName}();
$item.setName(name);
${ clazz.attributeCode(item) }
$executor.create${clazz.simpleName}($item${if (parentClazz == null) "" else ", $parent"});
${references.addAllReferences}
${item.updateAsClass(clazz.simpleName)}
""".trimIndent()

private val List<ObjectClass>.addAllReferences get() =
    joinToString("\n") { it.addReferenceEntry }

private val Class<*>.addReferenceEntry get() =
"""
if($pluralParameterName) {
    for each (ref in $pluralParameterName) {
        $item.add${this.simpleName}(ref);
    }
}
"""

private fun deleteScriptBody(className: String) = """
${item.retrieveExecutor}
${item.deleteAsClass(className)}
""".trimIndent()

private fun ForwardRelation.addReferenceRelationScriptBody() =
    if (simpleReference)
        addSimpleReferenceRelationScriptBody()
    else
        addRelationWithAttributeScriptBody()

private fun Property.setCode(ref: String) =
    "$ref.set${propertyName.capitalize()}($propertyName);"

private fun Property.propertyCode(prefix: String): String =
    if (clazz.isApiTypeClass) clazz.attributeCode(propertyName, prefix, true) else ""

private fun Class<*>.attributeCode(ref: String, prefix: String = "", init: Boolean = false) =
    properties.simpleProperties
        .filter(workflowPropertiesFilter)
        .run {
if (init)
"""
${prepare(prefix)}
var $ref = null;
if ($condition) {
    $ref = new Contrail$simpleName();
    ${assign(ref, prefix+tab)}
}
"""
else
"""
${prepare(prefix)}
${assign(ref, prefix)}
"""
}.trimStart().prependIndent(prefix)

val List<Property>.condition get() =
    joinToString(" ||\n$tab") { "${it.propertyName} != null" }

fun List<Property>.assign(ref: String, prefix: String = tab) =
    joinToString(separator = "\n$prefix") { it.setCode(ref) }

fun List<Property>.prepare(prefix: String) =
    joinToString(separator = "") { it.propertyCode(prefix) }

private fun ForwardRelation.addRelationWithAttributeScriptBody() = """
${ attribute.attributeCode("attribute") }
$parent.add$childName($child, attribute);
$retrieveExecutorAndUpdateParent
"""

private fun ForwardRelation.addSimpleReferenceRelationScriptBody() = """
$parent.add$childName($child);
$retrieveExecutorAndUpdateParent
"""

private fun ForwardRelation.removeReferenceRelationScriptBody() = """
${if (simpleReference)
    "$parent.remove$childName($child);"
else
    "$parent.remove$childName($child, null);"
}
$retrieveExecutorAndUpdateParent
""".trimIndent()

private val String.retrieveExecutor get() =
    "var $executor = ContrailConnectionManager.getExecutor($this.getInternalId().toString());"

private fun String.updateAsClass(className: String) =
    "$executor.update$className($this);"

private fun String.deleteAsClass(className: String) =
    "$executor.delete$className($this);"

private val ForwardRelation.updateParent get() =
    parent.updateAsClass(parentName)

private val ForwardRelation.retrieveExecutorAndUpdateParent get() =
"""
${parent.retrieveExecutor}
$updateParent
""".trimIndent()