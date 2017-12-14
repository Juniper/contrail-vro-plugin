/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.generator.CDATA
import net.juniper.contrail.vro.generator.addIfAbsent
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-paramType",
    propOrder = ["description", "parameterQualifiers"]
)
class PresentationParameter(
    @XmlAttribute(name = "name")
    val name: String? = null,

    descritpion: String? = null
) {
    @XmlElement(name = "desc", required = true)
    val description: String? = descritpion.CDATA

    @XmlElement(name = "p-qual")
    val parameterQualifiers: MutableList<ParameterQualifier> = mutableListOf()

    var mandatory: Boolean
        get() = parameterQualifiers.contains(ParameterQualifier.mandatory)
        set(value) {
            if (value)
                parameterQualifiers.addIfAbsent(ParameterQualifier.mandatory)
            else
                parameterQualifiers.remove(ParameterQualifier.mandatory)
        }

    fun addQualifiers(vararg qualifiers: ParameterQualifier) =
        parameterQualifiers.addAll(qualifiers)
}
