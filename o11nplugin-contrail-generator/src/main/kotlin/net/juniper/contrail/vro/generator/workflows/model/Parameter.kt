/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

import net.juniper.contrail.vro.generator.util.CDATA
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
    val name: String,

    type: ParameterType,

    description: String? = null
) {
    @XmlAttribute(name = "type")
    val type: String = type.name

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

sealed class ParameterType {
    abstract val name: String
}

object boolean : ParameterType() {
    override val name get() =
        "boolean"
}

object number : ParameterType() {
    override val name get() =
        "number"
}

object string : ParameterType() {
    override val name get() =
        "string"
}

object SecureString : ParameterType() {
    override val name get() =
        "SecureString"
}

class ReferenceParameter(val simpleName: String) : ParameterType() {
    constructor(clazz: Class<*>): this(clazz.simpleName)

    override val name: String get() =
        "Contrail:$simpleName"
}

val String.reference get() =
    ReferenceParameter(this)

val <T> Class<T>.reference get() =
    ReferenceParameter(this)

inline fun <reified T> reference() =
    ReferenceParameter(T::class.java)

