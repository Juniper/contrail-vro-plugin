/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.schema

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.config.parentType
import org.w3c.dom.Node

inline fun <reified T : Any> Schema.simpleTypeConstraints(propertyName: String): List<Constraint> =
    simpleTypeConstraints(T::class.java, propertyName)

fun Schema.simpleTypeConstraints(clazz: Class<*>, propertyName: String): List<Constraint> = when {
    clazz.isApiObjectClass -> objectFieldConstraints(clazz.xsdName, propertyName.xsdName)
    else -> propertyFieldConstraints(clazz, propertyName.xsdName)
}.toList()

inline fun <reified T : Any> Schema.propertyDescription(propertyName: String, convertToXsd: Boolean = true): String? =
    propertyDescription(T::class.java, propertyName, convertToXsd)

fun Schema.propertyDescription(clazz: Class<*>, propertyName: String, convertToXsd: Boolean = true): String? =
    propertyDescriptionImpl(clazz, propertyName.maybeToXsd(convertToXsd))

private fun Schema.propertyDescriptionImpl(clazz: Class<*>, xsdFieldName: String): String? = when {
    clazz.isApiObjectClass -> propertyDefinitionComment(clazz.xsdName, xsdFieldName).description
    else -> definitionNode(clazz, xsdFieldName).descriptionAttribute
}

fun Schema.objectDescription(clazz: ObjectClass, parent: ObjectClass? = null): String? {
    return relationDefinitionComment(parent?.xsdName ?: clazz.parentType ?: return null, clazz.xsdName).description
}

inline fun <reified F : Any, reified T : Any> Schema.relationDescription() =
    relationDescription(F::class.java, T::class.java)

fun Schema.relationDescription(from: Class<*>, to: Class<*>) =
    relationDefinitionComment(from, to).description

private fun Schema.objectFieldConstraints(xsdParent: String, xsdFieldName: String): Sequence<Constraint> {
    val fullName = "$xsdParent-$xsdFieldName"
    val matchingElements = elements.withAttribute(name) { it == xsdFieldName || it == fullName }
    if (matchingElements.size > 1)
        throw IllegalStateException("Multiple definitions of property $xsdFieldName")
    val definitionNode = matchingElements.firstOrNull() ?: return emptySequence()
    return constraintsOf(definitionNode)
}

private fun Schema.propertyFieldConstraints(clazz: Class<*>, xsdFieldName: String): Sequence<Constraint> =
    constraintsOf(definitionNode(clazz, xsdFieldName))

private fun Schema.definitionNode(clazz: Class<*>): Node =
    complexTypes.theOneNamed(clazz.simpleName)

private fun Schema.definitionNode(clazz: Class<*>, xsdFieldName: String): Node =
    definitionNode(clazz).nestedElements.theOneNamed(xsdFieldName)

private fun Schema.relationDefinitionComment(from: Class<*>, to: Class<*>): IdlComment =
    relationDefinitionComment(from.xsdName, to .xsdName)

private fun Schema.relationDefinitionComment(from: String, to: String): IdlComment =
    comments.asSequence().mapNotNull { it as? Link }.find { it.parentClassName == from && it.propertyClassName == to } ?:
        throw IllegalArgumentException("Relation $from-$to was not found in the schema.")

private fun Schema.propertyDefinitionComment(parent: String, propertyName: String): IdlComment =
    comments.firstOrNull { it.parentClassName == parent && (it.elementName == propertyName || it.elementName == "$parent-$propertyName") } ?:
        throw IllegalArgumentException("Property $propertyName of class $parent was not found in the schema.")

private fun Schema.basicConstraints(element: Node) =
    sequenceOf(required(element), defaultValue(element)).filterNotNull()

private fun Schema.specificConstraints(element: Node): Sequence<Constraint> {
    val elementType = element.typeAttribute ?: xsdString
    return if (elementType.isPrimitiveType)
        element.constraints(elementType)
    else
        simpleTypeConstraints(elementType)
}

private fun Schema.constraintsOf(element: Node): Sequence<Constraint> =
    basicConstraints(element) + specificConstraints(element)

private fun Schema.defaultValue(node: Node): Constraint? =
    node.attributeValue(default)
        ?.let { toTypedValueOf(node, it) }
        ?.let { DefaultValue(it) }

private fun Schema.toTypedValueOf(node: Node, value: String) =
    node.typedValueOf(primitiveTypeOf(node), value)

private fun Node.typedValueOf(primitiveType: String, value: String): Any = when (primitiveType) {
    xsdBoolean -> value.toBoolean()
    xsdInteger -> value.toInt()
    xsdString -> value
    else -> throw UnsupportedOperationException("Cannot create default value for element $typeAttribute.")
}

private fun required(node: Node): Constraint? =
    if (node.attributeValue(required) == "true") Required else null

private fun Node.constraints(type: String) = when (type) {
    xsdString -> stringConstraints()
    xsdInteger -> integerConstraints()
    else -> emptySequence()
}

private fun Schema.primitiveTypeOf(node: Node): String = when {
    node.nodeName == xsdSimpleType -> node.restrictionType
    node.nodeName == xsdElement -> node.typeAttribute?.let {
        if (it.isPrimitiveType) it else primitiveTypeOf(simpleTypes.theOneNamed(it))
    }
    else -> null
} ?: throw IllegalArgumentException("Unable to find primitive type of ${node.nameAttribute}.")

private fun Schema.simpleTypeConstraints(elementTypeName: String): Sequence<Constraint> =
    simpleTypes.theOneNamed(elementTypeName).
        restrictionNode.let { it.constraints(it.baseAttribute) }

private fun Node.integerConstraints(): Sequence<Constraint> =
    sequenceOf(minValue(), maxValue()).filterNotNull()

private fun Node.stringConstraints(): Sequence<Constraint> =
    sequenceOf(minLength(), maxLength(), pattern(), enumeration()).filterNotNull()

private fun Node.constraintNode(type: String) =
    children.firstOrNull { it.nodeName == type }

private inline fun Node.simpleConstraint(factory: (String) -> Constraint) =
    attributesMap[value]?.let(factory) ?:
        throw UnsupportedOperationException("Required attribute '$value' not found.")

private inline fun Node.simpleConstraint(nodeName: String, factory: (String) -> Constraint) =
    constraintNode(nodeName)?.simpleConstraint(factory)

private fun Node.minValue() =
    simpleConstraint(xsdMinInclusive) { MinValue(it.toLong()) }

private fun Node.maxValue() =
    simpleConstraint(xsdMaxInclusive) { MaxValue(it.toLong()) }

private fun Node.minLength() =
    simpleConstraint(xsdMinLength) { MinLength(it.toInt()) }

private fun Node.maxLength() =
    simpleConstraint(xsdMaxLength) { MaxLength(it.toInt()) }

private fun Node.pattern() =
    simpleConstraint(xsdPattern) { Regexp(it) }

private fun Node.enumeration() =
    children.filter { it.nodeName == xsdEnumeration }
        .mapNotNull { it.attributesMap[value] }
        .toList().let { if (it.isEmpty()) null else Enumeration(it) }
