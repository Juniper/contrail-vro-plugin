/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.config.asApiClass
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
import net.juniper.contrail.vro.generator.model.ClassProperties
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
import net.juniper.contrail.vro.generator.workflows.model.array
import net.juniper.contrail.vro.generator.workflows.model.createDunesProperties
import net.juniper.contrail.vro.generator.workflows.model.createElementInfoProperties
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string

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

private val String.descriptionFormat get() =
    replace(typeSuffix, "").splitCamel()

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

fun createWorkflow(info: ProjectInfo, className: String, parentName: String, refs: List<String>): Workflow {

    val workflowName = "Create ${className.workflowNameFormat}"

    val clazz = className.asApiClass!!

    return info.versionOf(workflowName) withScript createScriptBody(className, parentName, refs, clazz) andParameters {
        parameter("name", string) {
            description = "${className.descriptionFormat} name"
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
                        description = ref.folderName()
                    }
                }
            }
        }

        addProperties(clazz)
    }
}

fun deleteConnectionWorkflow(info: ProjectInfo): Workflow =
    deleteWorkflow(info, Connection, deleteConnectionScriptBody)

fun deleteWorkflow(info: ProjectInfo, className: String): Workflow =
    deleteWorkflow(info, className, deleteScriptBody(className))

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

private fun Property.toParameter(builder: ParameterAggregator) {
    builder.parameter(propertyName, clazz) {
        description = propertyName.splitCamel().capitalize()
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

private fun PresentationParametersBuilder.addProperties(properties: ClassProperties) {
    val topPrimitives = properties.simpleProperties.onlyPrimitives
    val topComplex = properties.simpleProperties.onlyComplex

    if (!topPrimitives.isEmpty()) {
        step("Custom Parameters") {
            topPrimitives.forEach { it.toParameter(this@step) }
        }
    }

    for (prop in topComplex) {
        val propProperties = prop.clazz.properties
        val primitives = propProperties.simpleProperties.onlyPrimitives
        val complex = propProperties.simpleProperties.onlyComplex

        when {
            primitives.isEmpty() -> groups(prop.title) {
                for (propProp in complex) {
                    group(propProp.title) {
                        propProp.clazz.properties.simpleProperties.forEach { it.toParameter(this@group) }
                    }
                }
            }
            complex.isEmpty() -> step(prop.title) {
                primitives.forEach { it.toParameter(this@step) }
            }
            else -> Unit // TODO("Add support for attributes with mixed structure.")
        }
    }
}

private fun PresentationParametersBuilder.addProperties(clazz: Class<*>) {
    addProperties(clazz.properties)
}

fun addReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation): Workflow {

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
            description = "${childName.descriptionFormat.capitalize()} to be added"
            mandatory = true
        }
        if ( ! relation.simpleReference) {
            addProperties(relation.attribute)
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
            dependsOn(parent)
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

private fun createScriptBody(className: String, parentName: String, references: List<String>, clazz: Class<*>) = """
${parent.retrieveExecutor}
var $item = new Contrail$className();
$item.setName(name);
${ clazz.attributeCode(item) }
$executor.create$className($item${if (parentName == Connection) "" else ", $parent"});
${references.addAllReferences}
${item.updateAsClass(className)}
""".trimIndent()

private val List<String>.addAllReferences get() =
    joinToString("\n") { it.addReferenceEntry }

private val String.addReferenceEntry get() =
"""
if($pluralParameterName) {
    for each (ref in $pluralParameterName) {
        $item.add$this(ref);
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