/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.format

import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Class that allows for defining custom formatting
 * for selected property classes.
 *
 * All methods in this class should be named "format"
 * and take single argument of selected property type.
 */
object PropertyFormatter {

    fun format(prop: KeyValuePair) =
        "${prop.key}: ${prop.value}"

    fun format(prop: SubnetType) =
        "${prop.ipPrefix}/${prop.ipPrefixLen}"

    fun format(prop: PortType) =
        if(prop.startPort == prop.endPort)
            "${prop.startPort}"
        else
            "${prop.startPort}-${prop.endPort}"

    fun format(prop: AddressType): String = prop.run {
        when {
            subnet != null -> format(subnet)
            virtualNetwork != null -> "VN: $virtualNetwork"
            securityGroup != null -> "SG: $securityGroup"
            networkPolicy != null -> "NP: $networkPolicy"
            subnetList != null -> format(subnetList)
            else -> ""
        }
    }

    private fun <T : ApiPropertyBase> List<T>?.format(transform: (T) -> CharSequence) =
        this?.joinToString(separator = ", ", transform = transform)

    private inline val List<AddressType>?.inline @JvmName("getInlineAddresses") get() =
        format { format(it) } ?: ""

    private inline val List<PortType>?.inline @JvmName("getInlinePorts") get() =
        format { format(it) } ?: ""

    fun format(prop: PolicyRuleType): String = prop.run {
        "$protocol  ${srcAddresses.inline} ${srcPorts.inline} $direction ${dstAddresses.inline} ${dstPorts.inline}"
    }

    fun format(prop: ApiPropertyBase):String =
        DefaultFormat.format(prop)

    fun <T : ApiPropertyBase> format(props: List<T>) =
        props.joinToString("\n") { format(it) }
}

private typealias Formatter = (Any?) -> String

object DefaultFormat {
    private val tab = "    "

    private val formatters = PropertyFormatter.javaClass.methods.asSequence()
        .filter { it.isCustomFormatter }
        .associateBy({ it.parameters[0].type }, { it.toFormatter() })


    fun format(obj: Any?, indent: String = ""): String {
        if (obj == null) return ""
        val fields = obj.javaClass.propertyFields

        return if (fields.size == 1 && fields[0].isPropertyListWrapper) {
            formatList(obj, fields[0], indent)
        } else {
            formatFields(obj, fields, indent)
        }
    }

    private fun formatList(obj: Any, field: Field, indent: String): String {
        val fieldValue = field.get(obj)
        return if (fieldValue is List<*>) {
            val listType = field.genericType.parameterClass!!
            val formatter = formatters[listType] ?: { format(it, indent)}

            fieldValue.joinToString(separator = "\n", transform = formatter)
        } else {
            ""
        }
    }

    private fun formatFields(obj: Any, fields: List<Field>, indent: String): String {
        val builder = StringBuilder()
        for (field in fields) {
            builder.append(indent).append(field.formatName()).append(":")
            val fieldValue = field.get(obj)
            if (fieldValue != null) {
                if (fieldValue is List<*>) {
                    if (fieldValue.isEmpty()) {
                        builder.append("\n")
                    } else {
                        builder.append("\n")
                        val nextIndent = indent + tab
                        for (element in fieldValue) {
                            builder.append(nextIndent).append(format(element))
                        }
                    }
                } else if (formatters.containsKey(field.type)) {
                    builder.append(formatters[field.type]!!.invoke(fieldValue))
                } else if (field.type.isCustomProperty) {
                    builder.append("\n").append(format(fieldValue, indent + tab))
                } else if (field.type == ApiPropertyBase::class.java) {
                    throw UnsupportedOperationException("Should not happen.")
                } else {
                    builder.append(" ").append(fieldValue).append("\n")
                }
            } else {
                builder.append("\n")
            }
        }
        return builder.toString()
    }

    private val Method.isCustomFormatter get() =
        name == "format" && parameterCount == 1 && parameters[0].type.isCustomProperty

    private val Class<*>.isCustomProperty get() =
        this != ApiPropertyBase::class.java && ApiPropertyBase::class.java.isAssignableFrom(this)

    private fun Method.toFormatter(): Formatter =
        { if (it == null) "" else invoke(PropertyFormatter, it) as String }

    private val Class<*>.propertyFields get() =
        declaredFields.asSequence()
            .filter { it.declaringClass == this }
            .onEach { it.isAccessible = true }
            .toList()

    private fun Field.formatName() =
        name.split("_").joinToString("") { it.capitalize() }.decapitalize()

    private val Field.isPropertyListWrapper get() =
        type == List::class.java && genericType.parameterClass?.isWrappableClass ?: false

    private val Class<*>.isWrappableClass get() =
        isCustomProperty || this == String::class.java

    private val Type.parameterClass: Class<*>? get() =
        parameterType?.unwrapped

    private val Type.parameterType: Type? get() =
        if (this is ParameterizedType) actualTypeArguments[0] else null

    private val Type.unwrapped: Class<*>? get() =
        if (this is ParameterizedType) rawType as? Class<*> else this as? Class<*>
}