/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.backReferenceClass
import net.juniper.contrail.vro.config.refsPropertyPrefix
import net.juniper.contrail.vro.config.refsPropertySuffix
import net.juniper.contrail.vro.config.isInReversedRelationTo
import net.juniper.contrail.vro.config.referenceClass
import net.juniper.contrail.vro.config.toPluginName
import java.lang.reflect.Method

class CustomReferenceProperty(
    val methodName: String,
    val refObjectType: String
) {
    val refObjectPluginType = refObjectType.toPluginName
    val propertyName = "$refsPropertyPrefix$refObjectPluginType$refsPropertySuffix"
    val wrapperMethodName = "get${propertyName.capitalize()}"
}

fun Method.toCustomReferenceProperty() = CustomReferenceProperty(
    methodName = name,
    refObjectType = referencePropertyClass!!.simpleName
)

val Method.referencePropertyClass get() =
    referenceClass ?: backReferenceClass

val Method.isReferenceProperty get() =
    referenceClass?.let { declaringClass.isInReversedRelationTo(it) } ?: false ||
    backReferenceClass?.let { !declaringClass.isInReversedRelationTo(it) } ?: false
