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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindingType",
    propOrder = ["binds"]
)
class Binding(binds: List<Bind>) {
    @XmlElement(name = "bind")
    val binds: List<Bind> = binds.toList()
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindType",
    propOrder = ["description"]
)
class Bind (
    name: String,
    type: ParameterType<Any>,
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
