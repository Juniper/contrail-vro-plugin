/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.allCapitalized
import net.juniper.contrail.vro.config.bold
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.config.isApiPropertyClass
import net.juniper.contrail.vro.config.parentType
import net.juniper.contrail.vro.config.typeToClassName
import org.w3c.dom.Node

val emptyAnswer = ""

inline fun <reified T : Any> Schema.simpleTypeConstraints(propertyName: String): List<Constraint> =
    simpleTypeConstraints(T::class.java, propertyName)

fun Schema.simpleTypeConstraints(clazz: Class<*>, propertyName: String, ignoreMissing: Boolean = false): List<Constraint> = when {
    clazz.isApiObjectClass -> objectFieldConstraints(clazz.xsdName, propertyName.xsdName)
    else -> propertyFieldConstraints(clazz, propertyName.xsdName, ignoreMissing)
}.toList()

inline fun <reified T : Any> Schema.crudStatus(propertyName: String) =
    crudStatus(T::class.java, propertyName)

fun Schema.crudStatus(clazz: Class<*>, propertyName: String): CRUD = when {
    clazz.isApiObjectClass -> propertyDefinitionComment(clazz.xsdName, propertyName.xsdName).crud
    else -> defaultCrud
}

inline fun <reified T : Any> Schema.predefinedAnswers(propertyName: String, mandatory: Boolean, convertToXsd: Boolean = true)
        : List<String> =
    predefinedAnswers(T::class.java, propertyName.maybeToXsd(convertToXsd), mandatory)

fun Schema.predefinedAnswers(clazz : Class<*>, xsdFieldName: String, mandatory: Boolean)
        : List<String> {
    val type = definitionNode(clazz, xsdFieldName).typeAttribute
    return nodeByName(type).restrictionNode.enumeration(mandatory)?.elements ?:
        throw IllegalStateException("No enumeration defined for this node")
}

inline fun <reified T : Any> Schema.propertyDescription(propertyName: String, convertToXsd: Boolean = true): String? =
    propertyDescription(T::class.java, propertyName, convertToXsd)

fun Schema.propertyDescription(clazz: Class<*>, propertyName: String, convertToXsd: Boolean = true): String? =
    propertyDescriptionImpl(clazz, propertyName.maybeToXsd(convertToXsd))

private fun Schema.propertyDescriptionImpl(clazz: Class<*>, xsdFieldName: String): String? = when {
    clazz.isApiObjectClass -> propertyDefinitionComment(clazz.xsdName, xsdFieldName).description
    else -> definitionNode(clazz, xsdFieldName).descriptionAttribute
}

fun Schema.classDescription(clazz: Class<*>, config: Config): String? = when {
    clazz.isApiObjectClass -> objectClassDefinitionComments(clazz, config).formatDescription()
    clazz.isApiPropertyClass -> propertyClassDefinitionComments(clazz, config).formatDescription()
    // wrapper over property class
    clazz.simpleName.contains('_') -> wrapperPropertyDefinitionComment(clazz)?.description
    else -> null
}

fun Schema.objectDescription(clazz: ObjectClass, config: Config, parent: ObjectClass? = null): String? {
    return relationDefinitionComment(parent?.xsdName ?: clazz.parentType(config) ?: return null, clazz.xsdName).description
}

inline fun <reified F : Any, reified T : Any> Schema.relationDescription() =
    relationDescription(F::class.java, T::class.java)

fun Schema.relationDescription(from: Class<*>, to: Class<*>, ignoreMissing: Boolean = false) =
    if (ignoreMissing)
        relationDefinitionCommentIfPresent(from, to)?.description
    else
        relationDefinitionComment(from, to).description

inline fun <reified F : ApiObjectBase, reified T : ApiObjectBase> createWorkflowDescription(schema : Schema, config: Config) =
    createWorkflowDescription(schema, T::class.java, config, F::class.java)

fun createWorkflowDescription(schema : Schema, clazz: ObjectClass, config: Config, parentClazz: ObjectClass? = null) : String? {
    val objectDescription = schema.objectDescription(clazz, config, parentClazz) ?: return null
    return """
${clazz.pluginName.allCapitalized.bold}
$objectDescription
""".trimIndent()
}

private fun Schema.objectFieldConstraints(xsdParent: String, xsdFieldName: String): Sequence<Constraint> {
    val fullName = "$xsdParent-$xsdFieldName"
    val matchingElements = elements.withAttribute(name) { it == xsdFieldName || it == fullName }
    if (matchingElements.size > 1)
        throw IllegalStateException("Multiple definitions of property $xsdFieldName")
    val definitionNode = matchingElements.firstOrNull() ?: return emptySequence()
    return constraintsOf(definitionNode, ignoreMissing = false)
}

private fun Schema.propertyFieldConstraints(clazz: Class<*>, xsdFieldName: String, ignoreMissing: Boolean): Sequence<Constraint> =
    constraintsOf(definitionNode(clazz, xsdFieldName), ignoreMissing)

private fun Schema.definitionNode(clazz: Class<*>): Node =
    complexTypes.theOneNamed(clazz.simpleName)

private fun Schema.definitionNode(clazz: Class<*>, xsdFieldName: String): Node =
    definitionNode(clazz).nestedElements.theOneNamed(xsdFieldName)

private fun Schema.relationDefinitionComment(from: Class<*>, to: Class<*>): Link =
    relationDefinitionComment(from.xsdName, to .xsdName)

private fun Schema.relationDefinitionCommentIfPresent(from: Class<*>, to: Class<*>): Link? =
    relationDefinitionCommentIfPresent(from.xsdName, to.xsdName)

private fun Schema.relationDefinitionCommentIfPresent(from: String, to: String): Link? =
    linkComments.find { it.parentClassName == from && it.propertyClassName == to }

private fun Schema.relationDefinitionComment(from: String, to: String): Link =
    relationDefinitionCommentIfPresent(from, to) ?:
        throw IllegalArgumentException("Relation $from-$to was not found in the schema.")

private fun Schema.objectClassDefinitionComments(clazz: Class<*>, config: Config): Sequence<Link> =
    clazz.methods.asSequence()
        .filter { it.name == "setParent" }
        .filter { it.parameterCount == 1 }
        .map { it.parameters[0].type }
        .filter { it.superclass == ApiObjectBase::class.java }
        .filter { config.isModelClass(it) }
        .map { relationDefinitionCommentIfPresent(it, clazz) }.filterNotNull()

private fun Schema.propertyClassDefinitionComments(clazz: Class<*>, config: Config): Sequence<Property> =
    elements.asSequence().filter { it.typeAttribute == clazz.simpleName }.map { it.nameAttribute }.filterNotNull()
        .flatMap { propertyComments.withElementName(it) }
        .filter { it.parentClassName.typeToClassName.run { isApiPropertyClass || config.isModelClassName(this) } }

private fun Schema.wrapperPropertyDefinitionComment(clazz: Class<*>): Property? {
    val parts = clazz.simpleName.split('_')
    val propertyName = parts.last().xsdName
    return propertyComments.withElementName(propertyName).firstOrNull()
}

private fun Schema.propertyDefinitionComment(parent: String, propertyName: String): IdlComment =
    comments.firstOrNull { it.parentClassName == parent && (it.elementName == propertyName || it.elementName == "$parent-$propertyName") } ?:
        throw IllegalArgumentException("Property $propertyName of class $parent was not found in the schema.")

private fun Schema.basicConstraints(element: Node) =
    sequenceOf(required(element), defaultValue(element)).filterNotNull()

private fun Schema.specificConstraints(element: Node, ignoreMissing: Boolean): Sequence<Constraint> {
    val elementType = element.typeAttribute ?: xsdString
    return if (elementType.isPrimitiveType)
        element.constraints(elementType)
    else
        simpleTypeConstraints(elementType, ignoreMissing)
}

private fun Schema.constraintsOf(element: Node, ignoreMissing: Boolean): Sequence<Constraint> =
    basicConstraints(element) + specificConstraints(element, ignoreMissing)

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

private fun Schema.simpleTypeConstraints(elementTypeName: String, ignoreMissing: Boolean = false): Sequence<Constraint> =
    simpleTypes.run { if (ignoreMissing) theOneNamedOrNull(elementTypeName) else theOneNamed(elementTypeName) }
        ?.restrictionNode?.let { it.constraints(it.baseAttribute) } ?: emptySequence()

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

private fun Node.enumeration(mandatory: Boolean = true) : Enumeration? {
    val list = children.filter { it.nodeName == xsdEnumeration }
        .mapNotNull { it.attributesMap[value] }.toMutableList()
    if (!mandatory && !list.isEmpty()) list.add(0, emptyAnswer)
    return list.let { if (it.isEmpty()) null else Enumeration(it) }
}

private fun Schema.nodeByName(name: String?) : Node =
    if (name != null) simpleTypes.theOneNamed(name) else throw IllegalStateException()
