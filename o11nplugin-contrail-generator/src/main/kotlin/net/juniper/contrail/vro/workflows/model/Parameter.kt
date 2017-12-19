/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.generator.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "paramType",
    propOrder = ["description"]
)
class Parameter(
    @XmlAttribute(name = "name")
    val name: String? = null,

    @XmlAttribute(name = "type")
    val type: String? = null,

    description: String? = null
) {
    @XmlElement(required = true)
    val description: String? = description.CDATA
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "parametersSetType",
    propOrder = ["parameters"]
)
class ParameterSet {
    @XmlElement(name = "param")
    private val parameters: MutableList<Parameter> = mutableListOf()

    fun addParameter(parameter: Parameter) =
        this.parameters.add(parameter)
}

