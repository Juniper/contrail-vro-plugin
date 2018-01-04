/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import net.juniper.contrail.vro.generator.workflows.model.Bind
import net.juniper.contrail.vro.generator.workflows.model.Parameter
import net.juniper.contrail.vro.generator.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.generator.workflows.model.ParameterSet
import net.juniper.contrail.vro.generator.workflows.model.ParameterType
import net.juniper.contrail.vro.generator.workflows.model.PresentationParameter

class ParameterInfo(
    val name: String,
    val type: ParameterType<Any>,
    val qualifiers: List<ParameterQualifier> = emptyList(),
    val description: String? = null
)

val ParameterInfo.asParameter get() =
    Parameter(name, type, description)

val List<ParameterInfo>.asParameters get() =
    map { it.asParameter }

val List<ParameterInfo>.asParameterSet get() =
    ParameterSet(asParameters)

val ParameterInfo.asPresentationParameter get() =
    PresentationParameter(name, description, qualifiers)

val List<ParameterInfo>.asPresentationParameters get() =
    map { it.asPresentationParameter }

val ParameterInfo.asBind get() =
    asBindWithExportName(name)

fun ParameterInfo.asBindWithExportName(exportName: String) =
    Bind(name, type, exportName, description)

val List<ParameterInfo>.asBinds get() =
    map { it.asBind }
