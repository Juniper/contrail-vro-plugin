/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model.info

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "properties",
    propOrder = arrayOf("entries")
)
@XmlRootElement(name = "properties")
class Properties {
    @XmlElement(name = "entry")
    val entries: MutableList<Entry> = mutableListOf()

    fun addEntries(vararg entries: Entry) {
        this.entries.addAll(entries)
    }

}