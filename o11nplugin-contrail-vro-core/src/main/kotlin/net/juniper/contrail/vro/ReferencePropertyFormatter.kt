/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldriven.converters.GenericToDateConverter
import com.vmware.o11n.sdk.modeldriven.converters.GenericToStringConverter
import net.juniper.contrail.api.ApiPropertyBase
import org.apache.commons.lang.WordUtils
import java.util.* // ktlint-disable no-wildcard-imports

class ReferencePropertyFormatter {

    fun <T : ApiPropertyBase> convert(attr: T?): String {
        if (attr == null || attr.javaClass.typeName.endsWith("ApiObjectbase"))
            return ""
        else
            return convertToString(attr, attr.javaClass.simpleName, "\t")

    }

    private fun convertToString(attribute: Any?, fieldName: String, indent: String): String {
        val lineFormat = "%s%s:\t%s\n"

        if (attribute == null)
            return ""

        // check if attribute has overriden toString method
        if (attribute::class.java.getMethod("toString").getDeclaringClass() !== Object::class.java) {
            return String.format(lineFormat, indent, reformatName(fieldName), attribute.toString())
        }

        if (GenericToStringConverter.supportedClasses().contains(attribute.javaClass)) {
            return String.format(lineFormat, indent, reformatName(fieldName), GenericToStringConverter().convert(attribute))
        }

        if (GenericToDateConverter.supportedClasses().contains(attribute.javaClass)) {
            return String.format(lineFormat, indent, reformatName(fieldName), GenericToDateConverter().convert(attribute))
        }

        val builder = StringBuilder()
        String.format(lineFormat, indent, reformatName(fieldName), "")
        for (field in attribute.javaClass.declaredFields) {
            field.isAccessible = true
            try {
                builder.append(convertToString(field.get(attribute), field.name, indent + "\t"))
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }

        val fieldTitle = String.format(lineFormat, indent, reformatName(fieldName), "")
        val nestedFileds = builder.toString()
        return if (nestedFileds != "") fieldTitle + nestedFileds else ""
    }

    private fun reformatName(name: String): String {
        val underscoreSplittedName = name.replace("_", " ")
        val splittedCammelCases = Arrays.asList(*underscoreSplittedName.split("(?<=[a-z])(?=[A-Z])".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        return WordUtils.capitalize(splittedCammelCases.joinToString(" ").toLowerCase())
    }
}
