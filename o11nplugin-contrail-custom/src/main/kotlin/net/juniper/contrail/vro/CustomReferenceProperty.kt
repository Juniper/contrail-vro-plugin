/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.DefaultConfig
import net.juniper.contrail.vro.config.backReferenceClass
import net.juniper.contrail.vro.config.refsPropertyPrefix
import net.juniper.contrail.vro.config.refsPropertySuffix
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
    referenceClass?.let {
        Config.getInstance(DefaultConfig).isInReversedRelationTo(declaringClass, it) ||
        Config.getInstance(DefaultConfig).isInPropertyRelationTo(declaringClass, it)
    } ?: false ||
    backReferenceClass?.let {
        Config.getInstance(DefaultConfig).isDisplayableChildOf(declaringClass, it) &&
        Config.getInstance(DefaultConfig).isInReversedRelationTo(declaringClass, it)
    } ?: false