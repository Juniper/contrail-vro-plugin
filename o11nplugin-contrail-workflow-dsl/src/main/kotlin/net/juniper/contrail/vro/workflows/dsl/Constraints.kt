/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.schema.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.schema.Regexp

fun Constraint.toQualifier(): ParameterQualifier = when (this) {

    Required -> mandatoryQualifier
    is DefaultValue<*> -> defaultValueQualifier(value.javaClass.parameterType, value)
    is MinValue -> minNumberValueQualifier(value)
    is MaxValue -> maxNumberValueQualifier(value)
    is Enumeration -> predefinedAnswersQualifier(string, elements)
    is MinLength -> minLengthQualifier(value)
    is MaxLength -> maxLengthQualifier(value)
    is Regexp -> regexQualifier(regexp)
}

