/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "positionType",
    propOrder = arrayOf("value")
)
class Position {

    @XmlValue
    var value: String? = null

    @XmlAttribute(name = "y")
    var y: String? = null

    @XmlAttribute(name = "x")
    var x: String? = null
}
