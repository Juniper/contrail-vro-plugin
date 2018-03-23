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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-qualType",
    propOrder = ["value"]
)
class ParameterQualifier(
    kind: QualifierKind,
    name: String,
    type: String? = null,
    value: String? = null
) {
    @XmlAttribute(name = "kind")
    val kind: String = kind.name

    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String? = type

    @XmlValue
    val value: String? = value.CDATA
}

enum class QualifierKind {
    static,
    ognl;
}

enum class QualifierName {
    mandatory,
    visible,
    defaultValue,
    genericEnumeration,
    dataBinding,
    ognlValidator,

    regexp,
    minStringLength,
    maxStringLength,
    textInput,
    sameValues,

    numberFormat,
    minNumberValue,
    maxNumberValue,

    sdkRootObject,
    linkedEnumeration,
    contextualParameter,
    showSelectAs { override fun toString() = "show-select-as" },

    beforeDate,
    afterDate,
}

enum class ReferenceSelector {
    list,
    tree,
    drop_down;

    override fun toString() =
        name.replace('_', '-')
}
