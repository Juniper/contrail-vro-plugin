/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.generator.CDATA
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

    companion object {
        val mandatory = ParameterQualifier("static", "mandatory", "boolean", true.toString())
    }
}

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