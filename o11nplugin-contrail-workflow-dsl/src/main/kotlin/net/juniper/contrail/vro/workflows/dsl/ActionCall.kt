/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

import net.juniper.contrail.vro.workflows.model.ActionParameter
import net.juniper.contrail.vro.workflows.model.ParameterType

infix fun <T : Any> String.ofType(type: ParameterType<T>) =
    ActionParameter(this, type)

class ActionCall private constructor(val name: String, vararg val arguments: String) {
    class ActionCallBuilder(val name: String) {
        private val arguments = mutableListOf<String>()

        fun parameter(parameter: String) = apply {
            arguments.add("#$parameter")
        }

        fun parameters(vararg parameters: String) = apply {
            parameters.forEach { parameter(it) }
        }

        fun string(string: String) = apply {
            arguments.add("\"$string\"")
        }

        fun boolean(bool: Boolean) = apply {
            arguments.add("$bool")
        }

        fun create() =
            ActionCall(name, *arguments.toTypedArray())

        fun snapshot() =
            ActionCallBuilder(name).also { it.arguments.addAll(arguments) }
    }
}

typealias ActionCallBuilder = ActionCall.ActionCallBuilder

fun actionCallTo(name: String) =
    ActionCallBuilder(name)