/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.workflows.model.wrapConstraints
import org.w3c.dom.Node

class ConstraintExtractor {
    private val SCHEMA_KNOWN_TYPES = arrayOf("xsd:string", "xsd:boolean", "xsd:integer")
    private val SUPPORTED_STRING_RESTRICTION_REGEX = Regex("xsd:(length|pattern|enumeration)")

    private val complexTypes = HashSet<Node>()
    private val simpleTypes = HashSet<Node>()
    private val elements = HashSet<Node>()
    private val idlComments = HashSet<IdlComment>()

    fun loadSchemas(vararg schemas: Schema) {
        loadSchemas(schemas.toList())
    }

    fun loadSchemas(schemas: List<Schema>) {
        val idlCommentsFactory = IdlCommentsFactory()
        for (schema in schemas) {
            val (complexTypes, simpleTypes, elements, comments) = schema.schemaChildren
            this.complexTypes += complexTypes
            this.simpleTypes += simpleTypes
            this.elements += elements
            this.idlComments += comments.map { idlCommentsFactory.buildFromComment(it.nodeValue) }.flatten()
        }
    }

    fun findSimpleTypeQualifiers(clazz: Class<*>, fieldName: String): List<ParameterQualifier> {
        return when {
            ApiObjectBase::class.java.isAssignableFrom(clazz) -> objectFiledQualifiers(fieldName)
            else -> propertyFiledQualifiers(clazz, fieldName)
        }
    }

    private fun objectFiledQualifiers(fieldName: String): List<ParameterQualifier> {
        val xsdFieldName = fieldName.replace("_", "-")
        val matchingElements = elements.withAttribute("name", xsdFieldName)
        if (matchingElements.size > 1) throw IllegalStateException("Schema is not compatible with api")
        return qualifiersFromXsdElement(matchingElements.first())
    }

    private fun propertyFiledQualifiers(clazz: Class<*>, fieldName: String): List<ParameterQualifier> {
        val complexType = complexTypes.find { it.attributesMap["name"] == clazz.simpleName }
        val elements = getNestedElements(complexType)

        val matchingElements = elements
            .withAttribute("name", fieldName.replace("_", "-"))

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
        xsdConstraints.putAll(elementAttributes.minus("name").minus("type"))

        val elementType = elementAttributes["type"] ?: throw IllegalStateException("Schema error")

        if (elementAttributes["type"] == "xsd:boolean" && elementAttributes["default"] != null) {
            xsdConstraints.put("default", elementAttributes["default"]!!.toBoolean())
        }

        if (elementAttributes["required"] != null) {
            xsdConstraints.put("required", elementAttributes["required"]!!.toBoolean())
        }

        if (elementType !in SCHEMA_KNOWN_TYPES) {
            val simpleTypeConstraints = extractSimpleTypeConstraints(elementType)
            xsdConstraints.putAll(simpleTypeConstraints)
        }

        return xsdConstraints.mapNotNull { (xsdConstraint, value) ->
            wrapConstraints(xsdConstraint, value)
        }
    }

    private fun extractSimpleTypeConstraints(elementType: String): HashMap<String, Any> {
        if (elementType.startsWith("smi:")) TODO("Implement constraints for smi namespace types")

        val matchingSimpleTypes = simpleTypes.withAttribute("name", elementType)
        if (matchingSimpleTypes.isEmpty()) throw IllegalArgumentException("Field is not a simple type")
        if (matchingSimpleTypes.size > 1) throw IllegalStateException("Error in schema") // error in schema

        val matchingNode = matchingSimpleTypes.first()
        val restrictionChild = matchingNode
            .children
            .find { it.nodeName == "xsd:restriction" }
        val baseType = restrictionChild?.attributesMap?.get("base") ?: throw IllegalStateException("Base is mandatory attribute")

        val xsdConstraints = HashMap<String, Any>()
        xsdConstraints.putAll(matchingNode.attributesMap.minus("name").minus("type"))
        xsdConstraints.putAll(restrictionChild.attributesMap.minus("name").minus("type"))
        return when (baseType) {
            "xsd:string" -> stringConstraints(restrictionChild)
            "xsd:integer" -> integerConstraints(restrictionChild)
            "xsd:boolean" -> TODO("Not sure if boolean has any restrictions")
            else -> HashMap()
        }

    }

    private fun integerConstraints(restrictionChild: Node): HashMap<String, Any> {
        val constraints = HashMap<String, Any>()
        val children = restrictionChild.children

        val maxInclusive =
            children.find { it.nodeName == "xsd:maxInclusive" }?.attributesMap?.get("value")?.toInt()
        if (maxInclusive != null) constraints.put("maxInclusive", maxInclusive)

        val minInclusive =
            children.find { it.nodeName == "xsd:minInclusive" }?.attributesMap?.get("value")?.toInt()
        if (minInclusive != null) constraints.put("minInclusive", minInclusive)

        return constraints
    }

    private fun stringConstraints(restrictionChild: Node): HashMap<String, Any> {
        val constraints = HashMap<String, Any>()
        val children = restrictionChild.children

        if (children.any { !it.nodeName.matches(SUPPORTED_STRING_RESTRICTION_REGEX) })
            TODO("Implement new restriction")

        val enumerations = children.filter { it.nodeName == "xsd:enumeration" }
        if (enumerations.isNotEmpty()) {
            constraints.put("enumerations", enumerations.mapNotNull { it.attributesMap["value"] })
        }

        val pattern = children.find { it.nodeName == "xsd:pattern" }?.attributesMap?.get("value")
        if (pattern != null) constraints.put("pattern", pattern)

        return constraints
    }
}