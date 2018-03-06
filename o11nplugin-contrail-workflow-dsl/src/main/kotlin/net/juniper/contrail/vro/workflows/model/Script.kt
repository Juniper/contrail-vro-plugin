/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
    name = "scriptType",
    propOrder = ["value"]
)
class Script(
    value: String,
    encoded: Boolean = false
) {
    @XmlValue
    val value: String = value.CDATA

    @XmlAttribute(name = "encoded")
    val encoded: String? = encoded.toString()

    @Transient
    val rawString = value
}
