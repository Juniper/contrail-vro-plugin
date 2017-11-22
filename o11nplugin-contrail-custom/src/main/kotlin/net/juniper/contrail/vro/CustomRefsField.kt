/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import org.apache.commons.lang.WordUtils

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class CustomRefsField {
    var name: String = ""
    var methodName: String = ""
    var returnTypeName: String = ""
    var refObjectType: String? = ""

    companion object {

        fun wrapField(field: Field): CustomRefsField {
            val customField = CustomRefsField()
            customField.name = field.name
            val fieldGenericType = field.genericType as ParameterizedType
            customField.returnTypeName = fieldGenericType.actualTypeArguments[0].toString()
            customField.methodName = generateMethodName(customField.name)
            customField.refObjectType = customField.methodName.replace("BackRefs", "")
            return customField
        }

        private fun generateMethodName(name: String?): String {
            var notSeparatedName = name
            if (!name!!.endsWith("back_refs")) {
                notSeparatedName = name.replace("_refs", "")
            }

            val separatedName = notSeparatedName!!.replace("_", " ").toLowerCase()
            return WordUtils.capitalize(separatedName).replace(" ", "")
        }
    }
}
