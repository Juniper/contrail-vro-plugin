/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

sealed class AdditionalProperty(val propertyName: String) {
    val methodName = "get${propertyName.capitalize()}"
}

object DisplayNameProperty : AdditionalProperty("displayName")

val propertyAsObjectNewProperties = listOf(DisplayNameProperty)