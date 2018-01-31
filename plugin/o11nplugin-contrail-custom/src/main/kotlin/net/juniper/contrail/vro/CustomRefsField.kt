/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.backRefTypeName
import net.juniper.contrail.vro.config.backRefsPropertyPrefix
import net.juniper.contrail.vro.config.toPluginName
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class CustomRefsField(
    val name: String,
    val returnTypeName: String,
    val refObjectType: String
) {
    val methodName: String = refObjectType+"BackRefs"
    val propertyName = "$backRefsPropertyPrefix${refObjectType.toPluginName}s"
    val wrapperMethodName = "get${propertyName.capitalize()}"

    companion object {
        fun wrapField(field: Field): CustomRefsField {
            val objectType = field.backRefTypeName
            val fieldGenericType = field.genericType as ParameterizedType
            val returnTypeName = fieldGenericType.actualTypeArguments[0].toString()
            return CustomRefsField(
                name = field.name,
                returnTypeName = returnTypeName,
                refObjectType = objectType
            )
        }

    }
}
