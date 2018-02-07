/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import net.juniper.contrail.vro.config.actionPackage
import net.juniper.contrail.vro.config.extractListProperty
import net.juniper.contrail.vro.config.propertyNotNull
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.workflows.model.QualifierKind.ognl
import net.juniper.contrail.vro.workflows.model.QualifierKind.static
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-qualType",
    propOrder = ["value"]
)
class ParameterQualifier(
    kind: QualifierKind,
    name: String,
    type: String? = null,
    value: String? = null
) {
    @XmlAttribute(name = "kind")
    val kind: String = kind.name

    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String? = type

    @XmlValue
    val value: String? = value.CDATA

}

enum class QualifierKind {
    static,
    ognl;
}

//TODO convert strings to sealed classes
val mandatoryQualifierName = "mandatory"
val visibleQualifierName = "visible"
val defaultValueQualifierName = "defaultValue"
val numberFormatQualifierName = "numberFormat"
val minNumberValueQualifierName = "minNumberValue"
val maxNumberValueQualifierName = "maxNumberValue"
val showInInventoryQualifierName = "contextualParameter"
val genericEnumerationQualifierName = "genericEnumeration"
val linkedEnumerationQualifierName = "linkedEnumeration"
val dataBindingQualifierName = "dataBinding"
val sdkRootObjectQualifierName = "sdkRootObject"
val selectAsQualifierName = "show-select-as"
val beforeDateQualifierName = "beforeDate"
val afterDateQualifierName = "afterDate"
val customValidatorQualifierName = "ognlValidator"
val multilineQualifierName = "textInput"

val voidValue = "__NULL__"

val showInInventoryQualifier = staticQualifier(showInInventoryQualifierName, void, voidValue)
val mandatoryQualifier = staticQualifier(mandatoryQualifierName, boolean, true)
val selectAsListQualifier = staticQualifier(selectAsQualifierName, string, "list")
val selectAsTreeQualifier = staticQualifier(selectAsQualifierName, string, "tree")
fun <T : Any> defaultValueQualifier(type: ParameterType<T>, value: T) = staticQualifier(defaultValueQualifierName, type, value)
fun <T : Any> predefinedAnswersQualifier(type: ParameterType<T>, values: List<T>): ParameterQualifier {
    val simpleType = type.unArrayed
    return staticQualifier(genericEnumerationQualifierName, array(simpleType), cDATAListFormat(simpleType, values))
}
fun <T : Any> predefinedAnswersActionQualifier(
    type: ParameterType<T>,
    action: ActionCall
): ParameterQualifier {
    val simpleType = type.unArrayed
    return ognlQualifier(
        genericEnumerationQualifierName,
        array(simpleType),
        action.ognl)
}

fun numberFormatQualifier(value: String) = staticQualifier(numberFormatQualifierName, string, value)
fun multilineQualifier() = staticQualifier(multilineQualifierName, void, voidValue)
fun minNumberValueQualifier(value: Long) = staticQualifier(minNumberValueQualifierName, number, value)
fun maxNumberValueQualifier(value: Long) = staticQualifier(maxNumberValueQualifierName, number, value)
fun visibilityConditionQualifier(condition: VisibilityCondition) =
    ognlQualifier(visibleQualifierName, boolean, condition.stringCondition)
fun listFromAction(action: Action) =
    ognlQualifier(linkedEnumerationQualifierName, action.resultType, action.ognl)
fun childOf(parent: String) =
    ognlQualifier(sdkRootObjectQualifierName, any, "#$parent")
fun bindDataTo(parameter: String, type: ParameterType<Any>) =
    ognlQualifier(dataBindingQualifierName, type, "#$parameter")
fun bindValueToNullableState(item: String, propertyPath: String) =
    ognlQualifier(dataBindingQualifierName, boolean, actionOgnl(actionPackage, propertyNotNull, "#$item", "\"$propertyPath\""))
fun <T : Any> bindValueToSimpleProperty(item: String, property: String, type: ParameterType<T>) =
    ognlQualifier(dataBindingQualifierName, type, "#$item.$property")
fun <T : Any> bindValueToComplexProperty(item: String, propertyPath: String, type: ParameterType<T>) =
    ognlQualifier(dataBindingQualifierName, type, actionOgnl(actionPackage, propertyValue, "#$item", "\"$propertyPath\""))
fun <T : Any> bindValueToListProperty(parentItem: String, childItem: String, listAccessor: String, propertyPath: String, type: ParameterType<T>) =
    ognlQualifier(dataBindingQualifierName, type, actionOgnl(
        packageName = actionPackage,
        name = extractListProperty,
        parameter = *arrayOf("#$parentItem", "#$childItem", "\"$listAccessor\"", "\"$propertyPath\"")
    ))
fun <T : Any> bindValueToAction(actionName: String, type: ParameterType<T>, vararg parameters: String) =
    ognlQualifier(dataBindingQualifierName, type, actionOgnl(actionPackage, actionName, *parameters))

fun cidrValidatorQualifier(parameter: String, packageName: String, actionName: String) =
    validatorActionQualifier(packageName, actionName, parameter)
fun allocValidatorQualifier(parameter: String, cidr: String, packageName: String, actionName: String) =
    validatorActionQualifier(packageName, actionName, cidr, parameter)

private val extractPropertyAction = "getPropertyValue"

private fun actionOgnl(packageName: String, name: String, vararg parameter: String) =
    """GetAction("$packageName","$name").call(${parameter.joinToString(",")})"""

val ActionCall.ognl get() =
    actionOgnl(actionPackage, name, *arguments)

private val Action.ognl get() =
    """GetAction("$packageName","$name").call($call)"""
private val Action.call get() =
    parameters.joinToString(",") { "#${it.name}" }

private fun <T : Any> staticQualifier(name: String, type: ParameterType<T>, value: T) =
    ParameterQualifier(static, name, type.name, value.toString())

private fun validatorActionQualifier(packageName: String, actionName: String, vararg parameters: String) =
    ognlQualifier(customValidatorQualifierName, string, actionOgnl(packageName, actionName, *parameters))

private fun ognlQualifier(name: String, type: ParameterType<Any>, value: String) =
    ParameterQualifier(ognl, name, type.name, value)

private fun <T : Any> cDATAListFormat(type: ParameterType<T>, values: List<T>): String {
    val elements = values.joinToString(";") { "#$type#$it#" }
    return "#{$elements}#"
}

val whitespaces = "\\s+".toRegex()

private fun String.cleanRegex() =
    replace(whitespaces, "")

fun wrapConstraints(xsdConstraint: String, constraintValue: Any): ParameterQualifier? =
    when (xsdConstraint) {
        "default" -> {
            ParameterQualifier(
                static,
                defaultValueQualifierName,
                constraintValue.javaClass.parameterType.name,
                constraintValue.toString()
            )
        }
        "minInclusive" -> minNumberValueQualifier(constraintValue.toString().toLong())
        "maxInclusive" -> maxNumberValueQualifier(constraintValue.toString().toLong())
        "pattern" -> {
            ParameterQualifier(
                static,
                "regexp",
                "Regexp",
                constraintValue.toString().cleanRegex()
            )
        }
        "enumerations" -> predefinedAnswersQualifier(string, constraintValue as List<String>)
        "required" -> mandatoryQualifier
        else -> null
    }

