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
    name = "workflow-itemType",
    propOrder = arrayOf("displayName", "script", "inBinding", "outBinding", "position")
)
class WorkflowItem(name: String, type: String) {

    @XmlElement(name = "display-name")
    var displayName: String? = null
    set( value ) {
        field = "<![CDATA[$value]]>"
    }

    @XmlElement
    var script: WorkflowScript? = null

    @XmlElement(name = "in-binding")
    var inBinding: InBinding? = null

    @XmlElement(name = "out-binding")
    var outBinding: OutBinding? = null

    @XmlElement(required = true)
    var position: Position? = null

    @XmlAttribute(name = "name")
    var name: String? = null

    @XmlAttribute(name = "type")
    var type: String? = null

    @XmlAttribute(name = "end-mode")
    var endMode: String? = null

    @XmlAttribute(name = "out-name")
    var outName: String? = null

    init {
        this.name = name
        this.type = type
    }
}
