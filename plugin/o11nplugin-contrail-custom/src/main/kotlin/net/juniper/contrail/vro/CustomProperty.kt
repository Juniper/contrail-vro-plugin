/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

class CustomProperty(
    val propertyType: String,
    val methodName: String
) {
    val viewMethodName = "$methodName$displayedPropertySuffix"
    val propertyName = methodName.methodToView()
    val viewPropertyName = viewMethodName.methodToView()

    private fun String.methodToView() =
        replaceFirst("get", "").decapitalize()
}

val displayedPropertySuffix = "View"
val displayedPropertyPattern = "$displayedPropertySuffix$".toRegex()
