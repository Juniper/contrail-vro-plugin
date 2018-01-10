/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldriven.AbstractWrapper
import com.vmware.o11n.sdk.modeldriven.Findable
import com.vmware.o11n.sdk.modeldriven.ModelWrapper
import com.vmware.o11n.sdk.modeldriven.Sid
import com.vmware.o11n.sdk.modeldriven.converters.GenericToDateConverter
import com.vmware.o11n.sdk.modeldriven.converters.GenericToStringConverter
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference

class ReferencePropertyFormatter(val factory: ContrailPluginFactory) {

    private val blankSpacesRegex = "^\\s*$".toRegex()
    private val lineFormat = "%s%s:\t%s\n"

    fun <T : ApiPropertyBase> getRefString(wrapper: AbstractWrapper, ref_list: List<ObjectReference<T>>?, type: String?): String? {
        if (ref_list == null) return null

        val builder = StringBuilder()
        var prefix = ""

        val wrapperSid: Sid?
        if (wrapper is Findable) wrapperSid = wrapper.getInternalId() else return ""

        try {
            for (ref in ref_list) {
                val sid = wrapperSid.with(type, ref.uuid)
                val element = factory.find(type, sid.toString()) as ModelWrapper?
                if (element != null) {
                    builder.append(prefix)
                    prefix = "\n"
                    builder.append((element.__getTarget() as ApiObjectBase).name)
                }
            }
        } catch (e: IllegalArgumentException) {
            return null
        }

        return builder.toString()
    }

    private fun <T : ApiPropertyBase> convert(attr: T?): String {
        if (attr == null || attr.javaClass.typeName.endsWith("ApiObjectbase"))
            return ""
        else
            return convertToString(attr, attr.javaClass.simpleName, "    ")
    }

    private fun convertToString(attribute: Any?, fieldName: String, indent: String): String {
        val builder = StringBuilder()

        if (attribute == null)
            return ""

        if (attribute is List<*>) {
            var listCounter = 0
            for ( listAttribute in attribute ) {
                builder.append(convertToString(listAttribute, reformatName("$fieldName [$listCounter]"), indent + "    "))
                listCounter++
            }

            val nestedFileds = builder.toString()
            return if (nestedFileds != "") nestedFileds else ""
        }

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

        for (field in attribute.javaClass.declaredFields) {
            field.isAccessible = true
            try {
                builder.append(convertToString(field.get(attribute), field.name, indent + "    "))
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
        val cammelCaseRegex = "(?<=[a-z])(?=[A-Z])".toRegex()
        val splittedCammelCases = underscoreSplittedName.split(cammelCaseRegex)
        return splittedCammelCases.joinToString(" ").toLowerCase().capitalize()
    }
}
