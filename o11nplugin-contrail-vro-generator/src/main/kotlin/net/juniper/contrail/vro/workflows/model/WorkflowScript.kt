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
    name = "scriptType",
    propOrder = arrayOf("value")
)
class WorkflowScript {

    @XmlValue
    var value: String? = null
    set( value ) {
        field = "<![CDATA[$value]]>"
    }

    @XmlAttribute(name = "encoded")
    var encoded: String? = null
}
