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
    name = "paramType",
    propOrder = arrayOf("description")
)
class Parameter {

    @XmlElement(required = true)
    var description: String? = null

    @XmlAttribute(name = "name")
    var name: String? = null

    @XmlAttribute(name = "type")
    var type: String? = null
}
