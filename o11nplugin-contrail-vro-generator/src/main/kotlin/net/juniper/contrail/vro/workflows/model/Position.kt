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
    propOrder = ["value"]
)
class Position (
    @XmlAttribute(name = "x")
    var x: String? = null,

    @XmlAttribute(name = "y")
    var y: String? = null,

    @XmlValue
    var value: String? = null
) {
    constructor(x: Float, y: Float) : this(x = x.toString(), y = y.toString())
}
