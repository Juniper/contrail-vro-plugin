package net.juniper.contrail.vro

sealed class AdditionalProperty(val propertyName: String) {
    val methodName = "get${propertyName.capitalize()}"
}

object DisplayNameProperty : AdditionalProperty("displayName")

val propertyAsObjectNewProperties = listOf(DisplayNameProperty)