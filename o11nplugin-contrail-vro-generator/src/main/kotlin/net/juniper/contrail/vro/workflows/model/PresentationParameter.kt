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
    name = "p-paramType",
    propOrder = arrayOf("description", "parameterQualifiers")
)
class PresentationParameter(name: String) {

    @XmlElement(name = "desc", required = true)
    var description: String? = null
    set( value ) {
        field = "<![CDATA[$value]]>"
    }

    @XmlElement(name = "p-qual")
    var parameterQualifiers: MutableList<ParameterQualifier> = mutableListOf()

    @XmlAttribute(name = "name")
    var name: String? = null

    init {
        this.name = name
    }

    constructor(name: String, rawDescritpiton: String) : this(name) {
        this.description = "<![CDATA[$rawDescritpiton]]>"
    }
}
