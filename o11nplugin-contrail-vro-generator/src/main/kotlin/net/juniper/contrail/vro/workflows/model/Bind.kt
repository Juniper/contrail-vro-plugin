/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.generator.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindType",
    propOrder = ["description"]
)
class Bind (
    @XmlAttribute(name = "name")
    val name: String? = null,

    @XmlAttribute(name = "type")
    val type: String? = null,

    @XmlAttribute(name = "export-name")
    val exportName: String? = null,

    description: String? = null
) {
    @XmlElement
    val description: String? = description.CDATA
}