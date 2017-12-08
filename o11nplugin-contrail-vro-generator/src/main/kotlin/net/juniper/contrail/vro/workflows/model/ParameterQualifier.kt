/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.generator.xsdType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-qualType",
    propOrder = arrayOf("value")
)
class ParameterQualifier(kind: String? = "static", name: String?, type: String?, rawValue: String?) {

    @XmlValue
    var value: String? = null

    @XmlAttribute(name = "kind")
    var kind: String? = null

    @XmlAttribute(name = "name")
    var name: String? = null

    @XmlAttribute(name = "type")
    var type: String? = null

    init {
        this.kind = kind
        this.name = name
        this.type = type
        this.value = "<![CDATA[$rawValue]]>"
    }
}

fun wrapConstraints(xsdConstraint: String, constraintValue: Any): ParameterQualifier? {
    return when (xsdConstraint) {
        "default" -> ParameterQualifier(
            kind = "static",
            name = "defaultValue",
            type = constraintValue.javaClass.xsdType,
            rawValue = "<![CDATA[$constraintValue]]>"
        )
        "minInclusive" -> ParameterQualifier(
            name = "minNumberValue",
            type = "number",
            rawValue = "<![CDATA[$constraintValue]]>"
        )
        "maxInclusive" -> ParameterQualifier(
            name = "maxNumberValue",
            type = "number",
            rawValue = "<![CDATA[$constraintValue]]>"
        )
        "pattern" -> ParameterQualifier(
            name = "regexp",
            type = "Regexp",
            rawValue = "<![CDATA[$constraintValue]]>"
        )
        "enumerations" -> ParameterQualifier(
            kind = "static",
            name = "genericEnumeration",
            type = "Array/string",
            rawValue = "<![CDATA[#{${(constraintValue as List<String>).joinToString(";") { "#string#$it#" }}}#]]>"
        )
        "required" -> ParameterQualifier(
            kind = "static",
            name = "mandatory",
            type = "boolean",
            rawValue = "<![CDATA[$constraintValue]]>"
        )
        else -> null
    }
}