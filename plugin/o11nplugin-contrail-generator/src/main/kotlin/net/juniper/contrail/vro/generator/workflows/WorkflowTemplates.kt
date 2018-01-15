/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.generator.model.ForwardRelation
import net.juniper.contrail.vro.generator.model.Property
import net.juniper.contrail.vro.generator.model.properties
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.parentClassName
import net.juniper.contrail.vro.config.pluralParameterName
import net.juniper.contrail.vro.config.splitCamel
import net.juniper.contrail.vro.config.underscoredPropertyToCamelCase
import net.juniper.contrail.vro.generator.workflows.dsl.ParameterAggregator
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
import java.lang.reflect.Field

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

private val String.workflowNameFormat
    get() =
    splitCamel().toLowerCase()

private val String.descriptionFormat
    get() =
    splitCamel()

private val <T : ApiObjectBase> Class<T>.parentName get() =
    parentClassName ?: Connection

val ProjectInfo.workflowVersion
    get() = "$baseVersion.$buildNumber"

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

    return info.versionOf(workflowName) withScript createScriptBody(className, parentName, refs) andParameters {
        parameter("name", string) {
            description = "${className.descriptionFormat} name"
            mandatory = true

        }
        parameter("parent", parentName.reference) {
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
    }
}

fun deleteConnectionWorkflow(info: ProjectInfo): Workflow =
    deleteWorkflow(info, Connection, deleteConnectionScriptBody)

fun deleteWorkflow(info: ProjectInfo, className: String): Workflow =
    deleteWorkflow(info, className, deleteScriptBody(className))

fun deleteWorkflow(info: ProjectInfo, className: String, scriptBody: String): Workflow {

    val workflowName = "Delete ${className.workflowNameFormat}"

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter("object", className.reference) {
            description = "${className.descriptionFormat} to delete"
            mandatory = true
            showInInventory = true
        }
    }
}

private val typeSuffix = "Type$".toRegex()

private val Property.title get() =
    clazz.simpleName.replace(typeSuffix, "").descriptionFormat

private fun Property.toParameter(builder: ParameterAggregator) {
    builder.parameter(propertyName, clazz) {
        description = propertyName.splitCamel().capitalize()
    }
}

fun addReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation): Workflow {

    val parentName = relation.parentName
    val childName = relation.childName
    val workflowName = "Add ${childName.workflowNameFormat} to ${parentName.workflowNameFormat}"
    val scriptBody = relation.addReferenceRelationScriptBody()

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter("parent", parentName.reference) {
            description = "${parentName.descriptionFormat.capitalize()} to add to"
            mandatory = true
        }
        parameter("child", childName.reference) {
            description = "${childName.descriptionFormat.capitalize()} to be added"
            mandatory = true
        }
        if ( ! relation.simpleReference) {
            val properties = relation.attribute.properties
            for (prop in properties.simpleProperties) {
                if (prop.clazz.isApiTypeClass) {
                    val propProperties = prop.clazz.properties
                    val primitives = propProperties.simpleProperties.filter { ! it.clazz.isApiTypeClass }
                    val complex = propProperties.simpleProperties.filter { it.clazz.isApiTypeClass }

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
                        else -> TODO("Add support for attributes with mixed structure.")
                    }

                } else {
                    prop.toParameter(this)
                }
            }
        }
    }
}

fun removeReferenceWorkflow(info: ProjectInfo, relation: ForwardRelation, action: Action): Workflow {

    val parentName = relation.parentName
    val childName = relation.childName
    val workflowName = "Remove ${childName.workflowNameFormat} from ${parentName.workflowNameFormat}"
    val scriptBody = relation.removeReferenceRelationScriptBody()

    return info.versionOf(workflowName) withScript scriptBody andParameters {
        parameter("parent", parentName.reference) {
            description = "${parentName.descriptionFormat.capitalize()} to remove from"
            mandatory = true
        }
        parameter("child", childName.reference) {
            description = "${childName.descriptionFormat.capitalize()} to be removed"
            mandatory = true
            dependsOn("parent")
            listedBy(action)
        }
    }
}

private val createConnectionScriptBody = """
var connectionId = ContrailConnectionManager.create(name, host, port, username, password, authServer, tenant);
System.log("Created connection with ID: " + connectionId);
""".trimIndent()

private val deleteConnectionScriptBody = """
ContrailConnectionManager.delete(object);
""".trimIndent()

private fun createScriptBody(className: String, parentName: String, references: List<String>) = """
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
var element = new Contrail$className();
element.setName(name);
${references.addAllReferences}
executor.create$className(element${if (parentName == Connection) "" else ", parent"});
""".trimIndent()

private val List<String>.addAllReferences get() =
    joinToString("\n") { it.addReferenceEntry }

private val String.addReferenceEntry get() =
"""
if($pluralParameterName) {
    for each (ref in $pluralParameterName) {
        element.add$this(ref);
    }
}
"""

private fun deleteScriptBody(className: String) = """
var executor = ContrailConnectionManager.getExecutor(object.getInternalId().toString());
executor.delete$className(object);
""".trimIndent()

private fun ForwardRelation.addReferenceRelationScriptBody() =
    if (simpleReference)
        addSimpleReferenceRelationScriptBody()
    else
        addRelationWithAttributeScriptBody()

private val Field.parameterName get() =
    name.underscoredPropertyToCamelCase()

private fun Field.setCode(ref: String) =
    "\n$ref.set${parameterName.capitalize()}($parameterName);"

private fun Class<*>.attributeCode(ref: String) =
    declaredFields.joinToString("\n") { it.attributeCode(ref) }

private fun Field.attributeCode(ref: String): String =
    if (type.isApiTypeClass) {
        """
var $parameterName = new Contrail${type.simpleName}();
${type.attributeCode(parameterName)}
        """.trimIndent()
    } else {
        ""
    } + setCode(ref)

private fun ForwardRelation.addRelationWithAttributeScriptBody() = """
var attribute = new Contrail$attributeSimpleName();
${ attribute.attributeCode("attribute") }
parent.add$childName(child, attribute);
$updateParent
"""

private fun ForwardRelation.addSimpleReferenceRelationScriptBody() = """
parent.add$childName(child);
$updateParent
"""

private fun ForwardRelation.removeReferenceRelationScriptBody() = """
${if (simpleReference)
    "parent.remove$childName(child);"
else
    "parent.remove$childName(child, null);"
}
$updateParent
""".trimIndent()

private val ForwardRelation.updateParent get() = """
var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.update$parentName(parent);
""".trimIndent()