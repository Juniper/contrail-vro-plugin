/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

data class AttributeDefinition(
    val name: String,
    val type: ParameterType<Any>,
    val description: String? = null,
    val readOnly: Boolean = false
) {
    fun toAttribute() = Attribute(
        name,
        type,
        description,
        readOnly
    )
}

val List<AttributeDefinition>.asAttributes get() =
    map { it.toAttribute() }

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "attributeType",
    propOrder = ["description"]
)

class Attribute(
    name: String,
    type: ParameterType<Any>,
    description: String? = null,
    readOnly: Boolean = false
) {
    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlAttribute(name = "read-only")
    val readOnly: Boolean = readOnly

    @XmlElement(name = "description")
    val description: String? = description.CDATA
}