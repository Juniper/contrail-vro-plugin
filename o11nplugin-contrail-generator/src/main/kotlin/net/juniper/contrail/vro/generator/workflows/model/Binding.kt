/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

import net.juniper.contrail.vro.generator.util.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindingType",
    propOrder = ["binds"]
)
class Binding {

    @XmlElement(name = "bind")
    private val binds: MutableList<Bind> = mutableListOf()

    fun bind(name: String, type: ParameterType, exportName: String = name) =
        apply { binds.add(Bind(name, type, exportName)) }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindType",
    propOrder = ["description"]
)
class Bind (
    name: String,
    type: ParameterType,
    exportName: String,
    description: String? = null
) {
    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlAttribute(name = "export-name")
    val exportName: String = exportName

    @XmlElement
    val description: String? = description.CDATA
}
