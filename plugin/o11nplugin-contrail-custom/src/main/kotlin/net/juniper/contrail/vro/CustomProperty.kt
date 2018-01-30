/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.displayedPropertySuffix

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
