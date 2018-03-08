/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.BackRefs
import net.juniper.contrail.vro.config.backRefTypeName
import net.juniper.contrail.vro.config.backRefsPropertyPrefix
import net.juniper.contrail.vro.config.toPluginName
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class CustomReferenceProperty(
    val name: String,
    val returnTypeName: String,
    val refObjectType: String
) {
    val methodName: String = "$refObjectType$BackRefs"
    val propertyName = "$backRefsPropertyPrefix${refObjectType.toPluginName}s"
    val wrapperMethodName = "get${propertyName.capitalize()}"
    val refObjectPluginType = refObjectType.toPluginName

    companion object {
        fun wrapField(field: Field): CustomReferenceProperty {
            val objectType = field.backRefTypeName
            val fieldGenericType = field.genericType as ParameterizedType
            val returnTypeName = fieldGenericType.actualTypeArguments[0].toString()
            return CustomReferenceProperty(
                name = field.name,
                returnTypeName = returnTypeName,
                refObjectType = objectType
            )
        }

    }
}
