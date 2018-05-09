/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests

import net.juniper.contrail.vro.model.utils
import net.juniper.contrail.vro.workflows.model.Action
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

const val dummyVarName = "dummy"
const val utilsName = "ContrailUtils"

object EngineManager {

    @JvmStatic
    val manager = ScriptEngineManager()

    var counter = 0
        get() = field++
        private set

    val engine: ScriptEngine get() =
        manager.getEngineByName("nashorn")
}

class ScriptTestEngine {
    val engine = EngineManager.engine

    private fun nextVarName() : String =
        dummyVarName + EngineManager.counter

    private fun invokeFunctionImpl(name: String, vararg args: Any?) : Any? {
        engine as Invocable
        return engine.invokeFunction(name, *args)
    }

    @JvmOverloads
    fun addToContext(name: String, value: Any? = null, scope: Int = ScriptContext.ENGINE_SCOPE) {
        val attr = value ?: name.getValue()
        engine.context.setAttribute(name, attr, scope)
    }

    fun evalFunction(function : String) {
        engine.eval(function, engine.context)
    }

    fun invokeFunction(name: String, vararg args: Any?) : Any? =
        invokeFunctionImpl(name, *args)

    fun invokeFunction(name: String, arg: Any?) : Any? =
        invokeFunctionImpl(name, arg)

    fun getFunctionFromActionScript(actions: List<Action>, name: String) : String {
        val varName = nextVarName()
        evalFunction(actions.getActionByName(name).provisionActionFunction(varName))
        return varName
    }
}

fun List<Action>.getActionByName(name: String) : Action =
    find { it.name == name } ?: throw IllegalArgumentException()

fun Action.provisionActionFunction(name : String) : String =
    """ var $name = function(${parameters.joinToString ( ", " ) { it.name }}) {
        ${script.rawString}};"""

fun String.getValue() : Any? =
    when (this) {
        utilsName -> utils
        else -> null
    }
