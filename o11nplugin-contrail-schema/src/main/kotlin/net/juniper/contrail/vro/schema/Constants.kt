/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

val value = "value"
val name = "name"
val type = "type"
val base = "base"
val default = "default"
val required = "required"
val description = "description"
val schemaLocation = "schemaLocation"
val optional = "optional"

val xsdString = "xsd:string"
val xsdBoolean = "xsd:boolean"
val xsdInteger = "xsd:integer"
val xsdTime = "xsd:time"
val xsdDateTime = "xsd:dateTime"

val xsdElement = "xsd:element"
val xsdSimpleType = "xsd:simpleType"
val xsdComplexType = "xsd:complexType"
val xsdAll = "xsd:all"
val xsdInclude = "xsd:include"

val xsdRestriction = "xsd:restriction"
val xsdEnumeration = "xsd:enumeration"
val xsdPattern = "xsd:pattern"
val xsdMinLength = "xsd:minLength"
val xsdMaxLength = "xsd:maxLength"
val xsdMinInclusive = "xsd:minInclusive"
val xsdMaxInclusive = "xsd:maxInclusive"

val xsdTypes = listOf(xsdComplexType, xsdSimpleType, xsdElement)
val primitiveTypes = listOf(xsdString, xsdBoolean, xsdInteger, xsdTime, xsdDateTime)

val String.isPrimitiveType get() =
    this in primitiveTypes

val ifmapIdlName = "#IFMAP-SEMANTICS-IDL"
val commentName = "#comment"
val textName = "#text"
val idlLink = "Link"
val idlProperty = "Property"
val idlListProperty = "ListProperty"