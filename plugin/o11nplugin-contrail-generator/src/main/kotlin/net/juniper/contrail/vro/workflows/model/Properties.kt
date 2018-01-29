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
    //TODO this property should be immutable
    private val entries: MutableList<Entry> = mutableListOf()

    fun addEntry(key: String, value: String) {
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

val categoryPathKey = "categoryPath"
val typeKey = "type"
val nameKey = "name"
val idKey = "id"
val pkgDescriptionKey = "pkg-description"
val pkgNameKey = "pkg-name"
val usedPluginsKey = "used-plugins"
val pkgOwnerKey = "pkg-owner"
val pkgIdKey = "pkg-id"

fun createElementInfoProperties(
    categoryPath: String,
    type: ElementType,
    name: String,
    id: String
): Properties = Properties().apply {
    addEntry(categoryPathKey, categoryPath)
    addEntry(typeKey, type.toString())
    addEntry(nameKey, name)
    addEntry(idKey, id)
}

fun createDunesProperties(
    pkgDescription: String,
    pkgName: String,
    usedPlugins: String,
    pkgOwner: String,
    pkgId: String
): Properties = Properties().apply {
    addEntry(pkgDescriptionKey, pkgDescription)
    addEntry(pkgNameKey, pkgName)
    addEntry(usedPluginsKey, usedPlugins)
    addEntry(pkgOwnerKey, pkgOwner)
    addEntry(pkgIdKey, pkgId)
}

