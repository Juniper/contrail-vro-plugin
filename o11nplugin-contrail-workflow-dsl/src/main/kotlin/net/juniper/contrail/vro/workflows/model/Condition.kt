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
    name = "conditionType",
    propOrder = ["value"]
)
class Condition (
    name: String,
    type: ParameterType<Any>,
    comparator: String,
    label: String,
    value: String
) {
    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlAttribute(name = "comparator")
    val comparator: String = comparator

    @XmlAttribute(name = "label")
    val label: String = label

    @XmlElement
    val value: String = value
}
