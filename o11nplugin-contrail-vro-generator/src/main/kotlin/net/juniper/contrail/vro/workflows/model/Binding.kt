/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "bindingType",
    propOrder = ["binds"]
)
class Binding {

    @XmlElement(name = "bind")
    private val binds: MutableList<Bind> = mutableListOf()

    fun bind(name: String, type: String, exportName: String = name) =
        apply { binds.add(Bind(name, type, exportName)) }
}
