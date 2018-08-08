/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.config.actionPackage
import net.juniper.contrail.vro.config.propertyNotNull
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.removeWhitespaces
import net.juniper.contrail.vro.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.QualifierKind
import net.juniper.contrail.vro.workflows.model.QualifierName
import net.juniper.contrail.vro.workflows.model.ReferenceSelector
import net.juniper.contrail.vro.workflows.model.Regexp
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.componentType
import net.juniper.contrail.vro.workflows.model.void

val voidValue = "__NULL__"

val mandatoryQualifier =
    staticQualifier(QualifierName.mandatory, boolean, true)

val showInInventoryQualifier =
    staticQualifier(QualifierName.contextualParameter, void, voidValue)

fun <T : Any> defaultValueQualifier(type: ParameterType<T>, value: T) =
    staticQualifier(QualifierName.defaultValue, type, value)

fun <T : Any> predefinedAnswersQualifier(type: ParameterType<T>, values: List<T>): ParameterQualifier {
    val simpleType = type.componentType
    return staticQualifier(QualifierName.genericEnumeration, simpleType.array, simpleType.formatList(values))
}

fun <T : Any> predefinedAnswersActionQualifier(type: ParameterType<T>, action: ActionCall) =
    ognlQualifier(QualifierName.genericEnumeration, type.componentType.array, action.ognl)

fun sameValuesQualifier(value: Boolean) =
    staticQualifier(QualifierName.sameValues, boolean, value)

fun numberFormatQualifier(value: String) =
    staticQualifier(QualifierName.numberFormat, string, value)

fun multilineQualifier() =
    staticQualifier(QualifierName.textInput, void, voidValue)

fun minNumberValueQualifier(value: Long) =
    staticQualifier(QualifierName.minNumberValue, number, value)

fun maxNumberValueQualifier(value: Long) =
    staticQualifier(QualifierName.maxNumberValue, number, value)

fun minLengthQualifier(value: Int) =
    staticQualifier(QualifierName.minStringLength, number, value)

fun maxLengthQualifier(value: Int) =
    staticQualifier(QualifierName.maxStringLength, number, value)

fun regexQualifier(pattern: String) =
    staticQualifier(QualifierName.regexp, Regexp, pattern.removeWhitespaces())

fun visibilityConditionQualifier(condition: VisibilityCondition) =
    ognlQualifier(QualifierName.visible, boolean, condition.stringCondition)

fun validationConditionQualifier(condition: ValidationCondition) =
    ognlQualifier(QualifierName.ognlValidator, boolean, condition.stringCondition)

fun <T : Any> listFromAction(action: ActionCall, type: ParameterType<T>) =
    ognlQualifier(QualifierName.linkedEnumeration, type, action.ognl)

fun selectWith(selector: ReferenceSelector) =
    staticQualifier(QualifierName.showSelectAs, string, selector.toString())

fun displayParentFrom(ognl: String) =
    ognlQualifier(QualifierName.sdkRootObject, any, ognl)

fun bindDataTo(parameter: String, type: ParameterType<Any>) =
    ognlQualifier(QualifierName.dataBinding, type, "#$parameter")

fun bindValueToNullableState(item: String, propertyPath: String) =
    ognlQualifier(QualifierName.dataBinding, boolean, actionOgnl(actionPackage, propertyNotNull, "#$item", "\"$propertyPath\""))

fun <T : Any> bindValueToSimpleProperty(item: String, property: String, type: ParameterType<T>) =
    ognlQualifier(QualifierName.dataBinding, type, "#$item.$property")

fun <T : Any> bindValueToComplexProperty(item: String, propertyPath: String, type: ParameterType<T>) =
    ognlQualifier(QualifierName.dataBinding, type, actionOgnl(actionPackage, propertyValue, "#$item", "\"$propertyPath\""))

fun <T : Any> bindValueToAction(action: ActionCall, type: ParameterType<T>) =
    ognlQualifier(QualifierName.dataBinding, type, action.ognl)

private fun actionOgnl(packageName: String, name: String, vararg parameters: String) =
    """GetAction("$packageName","$name").call(${ parameters.joinToString { it } })"""

val ActionCall.ognl get() =
    actionOgnl(actionPackage, name, *arguments)

private fun <T : Any> staticQualifier(name: QualifierName, type: ParameterType<T>, value: T) =
    ParameterQualifier(QualifierKind.static, name.toString(), type.name, value.toString())

private fun <T : Any> ognlQualifier(name: QualifierName, type: ParameterType<T>, value: String) =
    ParameterQualifier(QualifierKind.ognl, name.toString(), type.name, value)

private fun <T : Any> ParameterType<T>.formatList(values: List<T>): String {
    val elements = values.joinToString(";") { "#$this#$it#" }
    return "#{$elements}#"
}