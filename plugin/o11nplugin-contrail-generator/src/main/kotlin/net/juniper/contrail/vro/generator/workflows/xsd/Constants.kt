/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.xsd

val value = "value"
val name = "name"
val type = "type"
val default = "default"
val required = "required"
val description = "description"
val optional = "optional"

val xsdString = "xsd:string"
val xsdBoolean = "xsd:boolean"
val xsdInteger = "xsd:integer"

val xsdElement = "xsd:element"
val xsdSimpleType = "xsd:simpleType"
val xsdComplexType = "xsd:complexType"

val xsdInclude = "xsd:include"
val xsdRestriction = "xsd:restriction"
val xsdEnumeration = "xsd:enumeration"

val xsdTypes = listOf(xsdComplexType, xsdSimpleType, xsdElement)
val knownSchemaTypes = listOf(xsdString, xsdBoolean, xsdInteger)

val ifmapIdlName = "#IFMAP-SEMANTICS-IDL"
val commentName = "#comment"
val idlLink = "Link"
val idlProperty = "Property"

val stringRestrictionRegex = "xsd:(length|pattern|enumeration)".toRegex()