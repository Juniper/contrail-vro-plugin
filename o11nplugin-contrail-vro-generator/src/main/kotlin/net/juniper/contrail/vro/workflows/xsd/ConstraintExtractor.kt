/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.workflows.model.wrapConstraints
import org.w3c.dom.Node

private val VALUE = "value"
private val NAME = "name"
private val TYPE = "type"
private val DEFAULT = "default"
private val REQUIRED = "required"

private val XSD_STRING = "xsd:string"
private val XSD_BOOLEAN = "xsd:boolean"
private val XSD_INTEGER = "xsd:integer"

private val SCHEMA_KNOWN_TYPES = arrayOf(XSD_STRING, XSD_BOOLEAN, XSD_INTEGER)
private val SUPPORTED_STRING_RESTRICTION_REGEX = Regex("xsd:(length|pattern|enumeration)")

class ConstraintExtractor(private val schema: Schema) {

    fun findSimpleTypeQualifiers(clazz: Class<*>, fieldName: String): List<ParameterQualifier> =
        when {
            ApiObjectBase::class.java.isAssignableFrom(clazz) -> objectFiledQualifiers(fieldName)
            else -> propertyFiledQualifiers(clazz, fieldName)
        }

    private fun objectFiledQualifiers(fieldName: String): List<ParameterQualifier> {
        val xsdFieldName = fieldName.replace("_", "-")
        val matchingElements = schema.elements.withAttribute(NAME, xsdFieldName)
        if (matchingElements.size > 1) throw IllegalStateException("Schema is not compatible with api")
        return qualifiersFromXsdElement(matchingElements.first())
    }

    private fun propertyFiledQualifiers(clazz: Class<*>, fieldName: String): List<ParameterQualifier> {
        val complexType = schema.complexTypes.find { it.attributesMap[NAME] == clazz.simpleName }
        val elements = getNestedElements(complexType)

        val matchingElements = elements
            .withAttribute(NAME, fieldName.replace("_", "-"))

        if (matchingElements.size > 1) throw IllegalStateException()

        val matchingElement = matchingElements.first()
        return qualifiersFromXsdElement(matchingElement)
    }

    private fun getNestedElements(complexType: Node?): HashSet<Node> {
        val children = complexType?.children ?: HashSet<Node>()
        val elements = HashSet<Node>()
        elements.addAll(children)
        children.forEach { elements.addAll(getNestedElements(it)) }

        return elements.filter { it.nodeName == "xsd:element" }.toHashSet()
    }

    private fun qualifiersFromXsdElement(element: Node): List<ParameterQualifier> {
        val elementAttributes = element.attributesMap
        val xsdConstraints = HashMap<String, Any>()
        xsdConstraints.putAll(elementAttributes.minus(NAME).minus(TYPE))

        val elementType = elementAttributes[TYPE] ?: throw IllegalStateException("Schema error")

        if (elementAttributes[TYPE] == XSD_BOOLEAN && elementAttributes[DEFAULT] != null) {
            xsdConstraints.put(DEFAULT, elementAttributes[DEFAULT]!!.toBoolean())
        }

        if (elementAttributes[REQUIRED] != null) {
            xsdConstraints.put(REQUIRED, elementAttributes[REQUIRED]!!.toBoolean())
        }

        if (elementType !in SCHEMA_KNOWN_TYPES) {
            val simpleTypeConstraints = extractSimpleTypeConstraints(elementType)
            xsdConstraints.putAll(simpleTypeConstraints)
        }

        return xsdConstraints.mapNotNull { (xsdConstraint, value) ->
            wrapConstraints(xsdConstraint, value)
        }
    }

    private fun extractSimpleTypeConstraints(elementTypeName: String): HashMap<String, Any> {
        val elementType =
            if (elementTypeName.startsWith("smi:"))
                elementTypeName.substring(4)
            else
                elementTypeName

        val matchingSimpleTypes = schema.simpleTypes.withAttribute(NAME, elementType)
        if (matchingSimpleTypes.isEmpty()) throw IllegalArgumentException("Field is not a simple type")
        if (matchingSimpleTypes.size > 1) throw IllegalStateException("Error in schema") // error in schema

        val matchingNode = matchingSimpleTypes.first()
        val restrictionChild = matchingNode
            .children
            .find { it.nodeName == "xsd:restriction" }
        val baseType = restrictionChild?.attributesMap?.get("base") ?: throw IllegalStateException("Base is mandatory attribute")

        val xsdConstraints = HashMap<String, Any>()
        xsdConstraints.putAll(matchingNode.attributesMap.minus(NAME).minus(TYPE))
        xsdConstraints.putAll(restrictionChild.attributesMap.minus(NAME).minus(TYPE))
        return when (baseType) {
            XSD_STRING -> stringConstraints(restrictionChild)
            XSD_INTEGER -> integerConstraints(restrictionChild)
            else -> HashMap()
        }

    }

    private fun integerConstraints(restrictionChild: Node): HashMap<String, Any> {
        val constraints = HashMap<String, Any>()
        val children = restrictionChild.children

        val maxInclusive =
            children.find { it.nodeName == "xsd:maxInclusive" }?.attributesMap?.get(VALUE)?.toInt()
        if (maxInclusive != null) constraints.put("maxInclusive", maxInclusive)

        val minInclusive =
            children.find { it.nodeName == "xsd:minInclusive" }?.attributesMap?.get(VALUE)?.toInt()
        if (minInclusive != null) constraints.put("minInclusive", minInclusive)

        return constraints
    }

    private fun stringConstraints(restrictionChild: Node): HashMap<String, Any> {
        val constraints = HashMap<String, Any>()
        val children = restrictionChild.children

        if (children.any { !it.nodeName.matches(SUPPORTED_STRING_RESTRICTION_REGEX) }) {
            val unsupportedOperation = children
                .find { !it.nodeName.matches(SUPPORTED_STRING_RESTRICTION_REGEX) }
                ?.nodeName
            throw UnsupportedOperationException("Implement restriction: $unsupportedOperation")
        }

        val enumerations = children.filter { it.nodeName == "xsd:enumeration" }
        if (enumerations.isNotEmpty()) {
            constraints.put("enumerations", enumerations.mapNotNull { it.attributesMap[VALUE] })
        }

        val pattern = children.find { it.nodeName == "xsd:pattern" }?.attributesMap?.get(VALUE)
        if (pattern != null) constraints.put("pattern", pattern)

        return constraints
    }
}