/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

class Choice(val name: String, val targetId: Int)

@WorkflowBuilder
class ChoiceAggregator {
    private val _choices: MutableList<Choice> = mutableListOf()
    fun option(name: String, targetId: Int) {
        _choices.add(Choice(name, targetId))
    }

    val choices: List<Choice> = _choices
}

@WorkflowBuilder
class BindAggregator {
    private val _inBinds: MutableMap<String, String> = mutableMapOf()
    private val _outBinds: MutableMap<String, String> = mutableMapOf()
    fun inputBind(name: String, attributeName: String) {
        _inBinds[name] = attributeName
    }

    fun outputBind(name: String, attributeName: String) {
        _outBinds[name] = attributeName
    }

    val inBinds: Map<String, String> = _inBinds
    val outBinds: Map<String, String> = _outBinds
}

@WorkflowBuilder
class OutputAggregator {
    private val _output: MutableMap<String, String> = mutableMapOf()
    fun output(attribute: String, name: String) {
        _output[attribute] = name
    }

    val outputs: Map<String, String> = _output
}