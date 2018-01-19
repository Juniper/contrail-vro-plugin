/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.xsd

import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.generator.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.generator.workflows.model.wrapConstraints
import org.w3c.dom.Node

fun Schema.simpleTypeQualifiers(clazz: Class<*>, propertyName: String): List<ParameterQualifier> = when {
    clazz.isApiObjectClass -> objectFieldQualifiers(propertyName.propertyToXsd)
    else -> propertyFieldQualifiers(clazz, propertyName.propertyToXsd)
}

fun Schema.propertyDescription(clazz: Class<*>, propertyName: String): String? =
    definitionNode(clazz, propertyName.propertyToXsd).description

fun Schema.propertyFieldQualifiers(clazz: Class<*>, xsdFieldName: String): List<ParameterQualifier> =
    qualifiersOf(definitionNode(clazz, xsdFieldName))

fun Schema.definitionNode(clazz: Class<*>): Node =
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

fun Schema.qualifiersOf(element: Node): List<ParameterQualifier> {
    val elementAttributes = element.attributesMap
    val xsdConstraints = HashMap<String, Any>()
    xsdConstraints.putAll(elementAttributes.minus(name).minus(type))

    val elementType = elementAttributes[type] ?: throw IllegalStateException("Schema error")

    if (elementAttributes[type] == xsdBoolean && elementAttributes[default] != null) {
        xsdConstraints.put(default, elementAttributes[default]!!.toBoolean())
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

private fun Schema.simpleTypeConstraints(elementTypeName: String): HashMap<String, Any> {
    val elementType = elementTypeName.stripSmi

    val matchingSimpleTypes = simpleTypes.withAttribute(name, elementType)
    if (matchingSimpleTypes.isEmpty()) throw IllegalArgumentException("Field is not a simple type")
    if (matchingSimpleTypes.size > 1) throw IllegalStateException("Error in schema") // error in schema

    val matchingNode = matchingSimpleTypes.first()
    val restrictionChild = matchingNode
        .children
        .find { it.nodeName == xsdRestriction }
    val baseType = restrictionChild?.attributesMap?.get("base") ?: throw IllegalStateException("Base is mandatory attribute")

    val xsdConstraints = HashMap<String, Any>()
    xsdConstraints.putAll(matchingNode.attributesMap.minus(name).minus(type))
    xsdConstraints.putAll(restrictionChild.attributesMap.minus(name).minus(type))
    return when (baseType) {
        xsdString -> stringConstraints(restrictionChild)
        xsdInteger -> integerConstraints(restrictionChild)
        else -> HashMap()
    }
}

private fun integerConstraints(restrictionChild: Node): HashMap<String, Any> {
    val constraints = HashMap<String, Any>()
    val children = restrictionChild.children

    val maxInclusive =
        children.find { it.nodeName == "xsd:maxInclusive" }?.attributesMap?.get(value)?.toInt()
    if (maxInclusive != null) constraints.put("maxInclusive", maxInclusive)

    val minInclusive =
        children.find { it.nodeName == "xsd:minInclusive" }?.attributesMap?.get(value)?.toInt()
    if (minInclusive != null) constraints.put("minInclusive", minInclusive)

    return constraints
}

private fun stringConstraints(restrictionChild: Node): HashMap<String, Any> {
    val constraints = HashMap<String, Any>()
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

private fun Schema.objectFieldQualifiers(xsdFieldName: String): List<ParameterQualifier> {
    val matchingElements = elements.withAttribute(name, xsdFieldName)
    if (matchingElements.size > 1) throw IllegalStateException("Schema is not compatible with api")
    return qualifiersOf(matchingElements.first())
}
