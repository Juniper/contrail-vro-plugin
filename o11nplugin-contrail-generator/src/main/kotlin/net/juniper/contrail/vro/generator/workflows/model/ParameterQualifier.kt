/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

import net.juniper.contrail.vro.generator.util.CDATA
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
class ParameterQualifier (
    @XmlAttribute(name = "kind")
    val kind: String? = null,

    @XmlAttribute(name = "name")
    val name: String? = null,

    @XmlAttribute(name = "type")
    val type: String? = null,

    value: String? = null
) {
    @XmlValue
    val value: String? = value.CDATA

}

val staticKindName = "static"

val mandatoryQualifierName = "mandatory"
val defaultValueQualifierName = "defaultValue"
val numberFormatQualifierName = "numberFormat"
val minNumberValueQualifierName = "minNumberValue"
val maxNumberValueQualifierName = "maxNumberValue"
val showInInventoryQualifierName = "contextualParameter"

val booleanTypeName = "boolean"
val stringTypeName = "string"
val voidTypeName = "void"

val voidValue = "__NULL__"

val showInInventoryQualifier = ParameterQualifier(name = showInInventoryQualifierName, type = voidTypeName, value = voidValue)
val mandatoryQualifier = staticQualifier(mandatoryQualifierName, booleanTypeName, true.toString())
fun defaultValueQualifier(type: String, value: String) = staticQualifier(defaultValueQualifierName, type, value)
fun numberFormatQualifier(value: String) = staticQualifier(numberFormatQualifierName, stringTypeName, value)
fun minNumberValueQualifier(value: Int) = ParameterQualifier(name = minNumberValueQualifierName, value = value.toString())
fun maxNumberValueQualifier(value: Int) = ParameterQualifier(name = maxNumberValueQualifierName, value = value.toString())

private fun staticQualifier(name: String?, type: String?, value: String?) =
    ParameterQualifier(staticKindName, name, type, value)

fun wrapConstraints(xsdConstraint: String, constraintValue: Any): ParameterQualifier? =
    when (xsdConstraint) {
        "default" -> {
            ParameterQualifier(
                "static",
                "defaultValue",
                constraintValue.javaClass.xsdType,
                constraintValue.toString()
            )
        }
        "minInclusive" -> {
            ParameterQualifier(
                "minNumberValue",
                null,
                null,
                constraintValue.toString()
            )
        }
        "maxInclusive" -> {
            ParameterQualifier(
                "maxNumberValue",
                null,
                null,
                constraintValue.toString()
            )
        }
        "pattern" -> {
            ParameterQualifier(
                "static",
                "regexp",
                "Regexp",
                constraintValue.toString()
            )
        }
        "enumerations" -> {
            val xsdArrayAsString = (constraintValue as List<String>).joinToString(";") { "#string#$it#" }
            ParameterQualifier(
                "static",
                "genericEnumeration",
                "Array/string",
                xsdArrayAsString.toString()
            )
        }
        "required" -> {
            ParameterQualifier(
                "static",
                "mandatory",
                "boolean",
                constraintValue.toString()
            )
        }
        else -> null
    }

private val <T> Class<T>.xsdType: String?
    get() = TODO()