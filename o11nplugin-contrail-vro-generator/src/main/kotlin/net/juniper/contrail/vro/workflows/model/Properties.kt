/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "properties",
    propOrder = ["entries"]
)
@XmlRootElement(name = "properties")
class Properties {
    @XmlElement(name = "entry")
    private val entries: MutableList<Entry> = mutableListOf()

    fun addEntry(key: String, value:String) {
        entries.add(Entry(key, value))
    }

}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "entry",
    propOrder = ["key"]
)
private class Entry (
    @XmlAttribute(name = "key")
    val key: String?,

    @XmlValue
    val value: String?
)

fun properties(setup: PropertiesBuilder.()->Unit) : Properties =
    PropertiesBuilder().also(setup).build()

class PropertiesBuilder {
    private val entries = LinkedHashMap<String, String>()

    init {
        entries[typeKey] = Workflow.toString()
    }

    var type: ElementType
        get() = enumValueOf(entries[typeKey]!!)
        set(value) {
            entries[typeKey] = value.toString()
        }

    var categoryPath: String
        get() = entries[categoryPathKey] ?: ""
        set(value) {
            entries[categoryPathKey] = value
        }

    var name: String?
        get() = entries[nameKey]
        set(value) {
            entries[nameKey] = value ?: return
        }

    var id: String?
        get() = entries[idKey]
        set(value) {
            entries[idKey] = value ?: return
        }

    val Workflow get() =
        ElementType.Workflow

    fun build(): Properties =  Properties().apply {
        entries.forEach(::addEntry)
    }

    private companion object {
        val categoryPathKey = "categoryPath"
        val typeKey = "type"
        val nameKey = "name"
        val idKey = "id"
    }
}

enum class ElementType {
    Workflow
    //Action
}

