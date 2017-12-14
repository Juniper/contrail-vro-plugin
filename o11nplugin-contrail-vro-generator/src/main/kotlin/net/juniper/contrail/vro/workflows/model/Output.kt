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
    name = "parametersRootType",
    propOrder = ["parameters"]
)
class ParameterRoot {
    @XmlElement(name = "param")
    private val parameters: MutableList<Parameter> = mutableListOf()

    fun addParameter(parameter: Parameter) =
        this.parameters.add(parameter)
}
