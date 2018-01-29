/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.schema

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.defaultParentType
import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.workflows.model.wrapConstraints
import org.w3c.dom.Node

fun Schema.simpleTypeQualifiers(clazz: Class<*>, propertyName: String): List<ParameterQualifier> = when {
    clazz.isApiObjectClass -> objectFieldQualifiers(clazz.xsdName, propertyName.xsdName)
    else -> propertyFieldQualifiers(clazz, propertyName.xsdName)
}

inline fun <reified T : Any> Schema.propertyDescription(propertyName: String, convertToXsd: Boolean = true): String? =
    propertyDescription(T::class.java, propertyName, convertToXsd)

fun Schema.propertyDescription(clazz: Class<*>, propertyName: String, convertToXsd: Boolean = true): String? =
    propertyDescriptionImpl(clazz, propertyName.maybeToXsd(convertToXsd))

private fun Schema.propertyDescriptionImpl(clazz: Class<*>, xsdFieldName: String): String? = when {
    clazz.isApiObjectClass -> propertyDefinitionComment(clazz.xsdName, xsdFieldName).description
    else -> definitionNode(clazz, xsdFieldName).attributeValue(description)
}

fun Schema.objectDescription(clazz: ObjectClass): String? {
    return relationDefinitionComment(clazz.defaultParentType ?: return null, clazz.xsdName).description
}

inline fun <reified F : Any, reified T : Any> Schema.relationDescription() =
    relationDescription(F::class.java, T::class.java)

fun Schema.relationDescription(from: Class<*>, to: Class<*>) =
    relationDefinitionComment(from, to).description

private fun Schema.propertyFieldQualifiers(clazz: Class<*>, xsdFieldName: String): List<ParameterQualifier> =
    qualifiersOf(definitionNode(clazz, xsdFieldName))

private fun Schema.definitionNode(clazz: Class<*>): Node =
    complexTypes.find { it.attributeValue(name) == clazz.simpleName } ?:
        throw IllegalArgumentException("Class ${clazz.simpleName} was not found in the schema.")

private fun Schema.definitionNode(clazz: Class<*>, xsdFieldName: String): Node {
    val complexType = definitionNode(clazz)

    val nestedElements = complexType.nestedElements.toList()
    val matchingElements = nestedElements.withAttribute(name, xsdFieldName).toList()

    if (matchingElements.size > 1)
        throw IllegalStateException("Multiple definitions of ${clazz.simpleName} in the schema.")

    return matchingElements.firstOrNull() ?:
        throw IllegalArgumentException("Property $xsdFieldName of class ${clazz.simpleName} was not found in the schema.")
}

private fun Schema.relationDefinitionComment(from: Class<*>, to: Class<*>): IdlComment =
    relationDefinitionComment(from.xsdName, to .xsdName)

private fun Schema.relationDefinitionComment(from: String, to: String): IdlComment {
    val elementName = "$from-$to"
    return comments.find { it.elementName == elementName } ?:
        throw IllegalArgumentException("Relation $elementName was not found in the schema.")
}

private fun Schema.propertyDefinitionComment(parent: String, propertyName: String): IdlComment =
    comments.firstOrNull { it.parentClassName == parent && (it.elementName == propertyName || it.elementName == "$parent-$propertyName") } ?:
        throw IllegalArgumentException("Property $propertyName of class $parent was not found in the schema.")

private fun Schema.qualifiersOf(element: Node): List<ParameterQualifier> {
    val elementAttributes = element.attributesMap
    val xsdConstraints = mutableMapOf<String, Any>()
    xsdConstraints.putAll(elementAttributes.minus(name).minus(type))

    val elementType = elementAttributes[type] ?: throw IllegalStateException("Schema error")

    val defaultValue = elementAttributes[default]
    if (defaultValue != null) {
        @Suppress("IMPLICIT_CAST_TO_ANY")
        val typedValue = when {
            elementType == xsdBoolean -> defaultValue.toBoolean()
            elementType == xsdInteger -> defaultValue.toInt()
            defaultValue == "true" -> true
            defaultValue == "false" -> false
            else -> defaultValue.toIntOrNull() ?: defaultValue
        }
        xsdConstraints.put(default, typedValue)
    }

    if (elementAttributes[required] != null) {
        xsdConstraints.put(required, elementAttributes[required]!!.toBoolean())
    }

    if (elementType !in knownSchemaTypes) {
        val simpleTypeConstraints = simpleTypeConstraints(elementType)
        xsdConstraints.putAll(simpleTypeConstraints)
    }

    return xsdConstraints.mapNotNull { (xsdConstraint, value) ->
        wrapConstraints(xsdConstraint, value)
    }
}

private fun Schema.simpleTypeConstraints(elementTypeName: String): Map<String, Any> {
    val elementType = elementTypeName.stripSmi

    val matchingSimpleTypes = simpleTypes.withAttribute(name, elementType)
    if (matchingSimpleTypes.isEmpty())
        throw IllegalArgumentException("Property $elementType is not a simple type.")
    if (matchingSimpleTypes.size > 1)
        throw IllegalStateException("Multiple definitions of property $elementType.")

    val matchingNode = matchingSimpleTypes.first()
    val restrictionChild = matchingNode
        .children
        .find { it.nodeName == xsdRestriction }
    val baseType = restrictionChild?.attributesMap?.get("base") ?:
        throw IllegalStateException("Mandatory attribute 'base' was not found in definition of property $elementType.")

    val xsdConstraints = mutableMapOf<String, Any>()
    xsdConstraints.putAll(matchingNode.attributesMap.minus(name).minus(type))
    xsdConstraints.putAll(restrictionChild.attributesMap.minus(name).minus(type))
    return when (baseType) {
        xsdString -> stringConstraints(restrictionChild)
        xsdInteger -> integerConstraints(restrictionChild)
        else -> mutableMapOf()
    }
}

private fun integerConstraints(restrictionChild: Node): Map<String, Any> {
    val constraints = mutableMapOf<String, Any>()
    val children = restrictionChild.children

    val maxInclusive =
        children.find { it.nodeName == "xsd:maxInclusive" }?.attributesMap?.get(value)?.toLong()
    if (maxInclusive != null) constraints.put("maxInclusive", maxInclusive)

    val minInclusive =
        children.find { it.nodeName == "xsd:minInclusive" }?.attributesMap?.get(value)?.toLong()
    if (minInclusive != null) constraints.put("minInclusive", minInclusive)

    return constraints
}

private fun stringConstraints(restrictionChild: Node): Map<String, Any> {
    val constraints = mutableMapOf<String, Any>()
    val children = restrictionChild.children

    if (children.any { !it.nodeName.matches(stringRestrictionRegex) }) {
        val unsupportedOperation = children
            .find { !it.nodeName.matches(stringRestrictionRegex) }
            ?.nodeName
        throw UnsupportedOperationException("Implement restriction: $unsupportedOperation")
    }

    val enumerations = children.filter { it.nodeName == xsdEnumeration }.toList()
    if (enumerations.isNotEmpty()) {
        constraints.put("enumerations", enumerations.mapNotNull { it.attributesMap[value] })
    }

    val pattern = children.find { it.nodeName == "xsd:pattern" }?.attributesMap?.get(value)
    if (pattern != null) constraints.put("pattern", pattern)

    return constraints
}

private fun Schema.objectFieldQualifiers(xsdParent: String, xsdFieldName: String): List<ParameterQualifier> {
    val fullName = "$xsdParent-$xsdFieldName"
    val matchingElements = elements.withAttribute(name) { it == xsdFieldName || it == fullName }
    if (matchingElements.size > 1)
        throw IllegalStateException("Multiple definitions of property $xsdFieldName")
    val definitionNode = matchingElements.firstOrNull() ?: return emptyList()
    return qualifiersOf(definitionNode)
}
