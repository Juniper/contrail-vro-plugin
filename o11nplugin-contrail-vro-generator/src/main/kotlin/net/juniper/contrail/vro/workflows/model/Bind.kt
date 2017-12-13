/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindType",
    propOrder = arrayOf("description")
)
class Bind(name: String, type: String, exportName: String) {

    @XmlElement(required = true)
    var description: String? = null

    @XmlAttribute(name = "name")
    var name: String? = null

    @XmlAttribute(name = "type")
    var type: String? = null

    @XmlAttribute(name = "export-name")
    var exportName: String? = null

    init {
        this.name = name
        this.type = type
        this.exportName = exportName
    }
}
