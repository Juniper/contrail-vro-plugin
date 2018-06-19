/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.config.withoutCDATA
import net.juniper.contrail.vro.workflows.model.Bind
import net.juniper.contrail.vro.workflows.model.Parameter
import net.juniper.contrail.vro.workflows.model.ParameterQualifier
import net.juniper.contrail.vro.workflows.model.ParameterSet
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.PresentationParameter
import net.juniper.contrail.vro.workflows.model.Reference
import net.juniper.contrail.vro.workflows.model.Regexp
import net.juniper.contrail.vro.workflows.model.SecureString
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.date
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.pair
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.void

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

val List<ParameterInfo>.asReferences get() =
    asSequence()
        .filter { it.type is Reference }
        .filter { it.qualifiers.contains(showInInventoryQualifier) }
        .map { it.type as Reference }
        .distinct()
        .toList()

fun Parameter.toParameterInfo(qualifiers: List<ParameterQualifier> = emptyList()): ParameterInfo {
    val parameterType = type.toParameterType
    return ParameterInfo(name, parameterType, qualifiers, description?.withoutCDATA)
}

val String.toParameterType: ParameterType<Any> get() = when (this) {
    "string" -> string
    "number" -> number
    "boolean" -> boolean
    "SecureString" -> SecureString
    "Regexp" -> Regexp
    "Date" -> date
    "void" -> void
    "any" -> any
    else -> when {
        startsWith("CompositeType") -> {
            val params = substring(14, length - 1).split(",").flatMap { it.split(":") }
            val name1 = params[0]
            val type1 = params[1].toParameterType
            val name2 = params[2]
            val type2 = params[3].toParameterType
            pair(name1, type1, name2, type2)
        }
        startsWith("Array") ->
            array(split("/")[1].toParameterType)
        else -> {
            val parts = split(":")
            Reference(parts[1], parts[0])
        }
    }
}