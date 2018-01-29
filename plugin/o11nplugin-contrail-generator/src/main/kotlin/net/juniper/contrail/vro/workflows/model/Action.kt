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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "action",
    propOrder = ["description", "parameters", "script"]
)
@XmlRootElement(name = "dunes-script-module")
class Action(
    name: String,
    packageName: String,
    id: String,
    version: String,
    resultType: ParameterType<Any>,
    parameters: List<ActionParameter>,
    script: Script,
    description: String? = null
) : Element {
    // this constructor is only necessary to satisfy marshaller
    constructor(): this("dummyAction", "", "123456789", "1.0.0", void, emptyList(), Script(""))

    @XmlAttribute(name = "name")
    val name: String = name

    @Transient
    val packageName: String = packageName

    @Transient
    val resultType: ParameterType<Any> = resultType

    @XmlAttribute(name = "result-type")
    val resultTypeName: String = resultType.name

    @XmlAttribute(name = "id")
    override val id: String = id

    @XmlAttribute(name = "version")
    val version: String = version

    @XmlAttribute(name = "api-version")
    val apiVersion: String = "6.0.0"

    @XmlAttribute(name = "allowed-operations")
    val allowedOperations: String = "vef"

    @XmlElement(name = "description")
    val description: String? = description

    @XmlElement(name = "param")
    val parameters: List<ActionParameter> = parameters.toList()

    @XmlElement(name = "script")
    val script: Script = script

    override val outputName: String get() =
        name

    override val elementType: ElementType get() =
        ElementType.ScriptModule
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paramType" )
class ActionParameter(
    name: String,
    type: ParameterType<Any>
) {
    @XmlAttribute(name = "n")
    val name: String = name

    @XmlAttribute(name = "t")
    val type: String = type.name
}