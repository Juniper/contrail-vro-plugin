/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

class AdditionalProperty(val propertyName: String) {
    val methodName = "get${propertyName.capitalize()}"
}

val displayNameProperty = AdditionalProperty("displayName")

val propertyAsObjectNewProperties get() = listOf(displayNameProperty)