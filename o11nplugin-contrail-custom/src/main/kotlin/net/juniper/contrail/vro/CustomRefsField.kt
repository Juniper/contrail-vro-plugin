/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class CustomRefsField(
    val name: String,
    val returnTypeName: String,
    val refObjectType: String
) {
    val methodName: String = refObjectType+"BackRefs"
    val wrapperMethodName = "Associated${refObjectType}s"

    companion object {
        private val backRefsPattern = "_back_refs$".toRegex()

        fun wrapField(field: Field): CustomRefsField {
            val objectType = extractObjectName(field)
            val fieldGenericType = field.genericType as ParameterizedType
            val returnTypeName = fieldGenericType.actualTypeArguments[0].toString()
            return CustomRefsField(
                name = field.name,
                returnTypeName = returnTypeName,
                refObjectType = objectType
            )
        }

        private fun extractObjectName(field: Field): String {
            val base = field.name.replace(backRefsPattern, "")
            return base.split("_").joinToString("") { it.toLowerCase().capitalize() }
        }
    }
}
