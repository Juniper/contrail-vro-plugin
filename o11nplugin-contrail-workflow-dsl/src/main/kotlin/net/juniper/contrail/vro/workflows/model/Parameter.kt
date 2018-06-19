/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import net.juniper.contrail.vro.config.constants.Contrail
import net.juniper.contrail.vro.config.pluginName
import java.util.Date
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
    name: String,
    type: ParameterType<Any>,
    description: String? = null
) {
    @XmlAttribute(name = "name")
    val name: String = name

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
class ParameterSet(parameters: List<Parameter> = emptyList()) {
    @XmlElement(name = "param")
    val parameters: List<Parameter> = parameters.toList()
}

sealed class ParameterType<out Type : Any> {
    abstract val name: String
    override fun toString() =
        name
}

object boolean : ParameterType<Boolean>() {
    override val name get() =
        "boolean"
}

object number : ParameterType<Long>() {
    override val name get() =
        "number"
}

object string : ParameterType<String>() {
    override val name get() =
        "string"
}

object SecureString : ParameterType<String>() {
    override val name get() =
        "SecureString"
}

object Regexp : ParameterType<String>() {
    override val name get() =
        "Regexp"
}

object date : ParameterType<Date>() {
    override val name get() =
        "Date"
}

object void : ParameterType<void>() {
    override val name get() =
        "void"
}

object any : ParameterType<Any>() {
    override val name get() =
        "any"
}

data class pair<Type1 : Any, Type2 : Any>(
    val name1: String, val type1: ParameterType<Type1>,
    val name2: String, val type2: ParameterType<Type2>
) : ParameterType<Pair<Type1, Type2>>() {
    override val name: String get() =
        "CompositeType($name1:${type1.name},$name2:${type2.name})"

    //just to avoid the auto-generated data class version of toString()
    override fun toString() =
        name
}

data class array<out Type : Any>(val type: ParameterType<Type>) : ParameterType<List<Type>>() {
    override val name: String get() =
        "Array/${type.name}"

    //just to avoid the auto-generated data class version of toString()
    override fun toString() =
        name
}

data class Reference(val simpleName: String, val plugin: String = Contrail) : ParameterType<Reference>() {
    constructor(clazz: Class<*>): this(clazz.pluginName)

    override val name: String get() =
        "$plugin:$simpleName"

    //just to avoid the auto-generated data class version of toString()
    override fun toString() =
        name
}

val <Type : Any> ParameterType<Type>.array get() =
    array<Type>(this)

val <Type : Any> ParameterType<Type>.componentType: ParameterType<Any> get() =
    if (this is array<Any>) type.componentType else this

val String.reference get() =
    Reference(this)

val Class<*>.reference get() =
    Reference(this)

inline fun <reified T> reference() =
    T::class.java.reference

val Class<*>.parameterType get() = when (this) {
    java.lang.Boolean::class.java, java.lang.Boolean.TYPE -> boolean
    String::class.java -> string
    java.lang.Integer::class.java, java.lang.Integer.TYPE,
    java.lang.Long::class.java, java.lang.Long.TYPE -> number
    java.util.Date::class.java -> date
    else -> reference
}

val String.toParameterType: ParameterType<Any> get() = when (this) {
    string.name -> string
    number.name -> number
    boolean.name -> boolean
    SecureString.name -> SecureString
    Regexp.name -> Regexp
    date.name -> date
    void.name -> void
    any.name -> any
    else -> when {
        startsWith("CompositeType") -> {
            val params = removePrefix("CompositeType(").removeSuffix(")").split(",").map { it.split(":") }
            val name1 = params[0][0]
            val type1 = params[0][1].toParameterType
            val name2 = params[1][0]
            val type2 = params[1][1].toParameterType
            pair(name1, type1, name2, type2)
        }
        startsWith("Array") ->
            array(removePrefix("Array/").toParameterType)
        else -> {
            val parts = split(":")
            Reference(parts[1], parts[0])
        }
    }
}