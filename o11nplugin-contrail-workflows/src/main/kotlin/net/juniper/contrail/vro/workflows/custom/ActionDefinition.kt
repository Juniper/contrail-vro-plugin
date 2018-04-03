/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.ActionParameter
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.util.generateID

class ActionDefinition(
    val name : String,
    val resultType: ParameterType<Any>,
    val parameters: List<ActionParameter>,
    val script: String = ScriptLoader.loadActionScript(name)
)

operator fun ActionDefinition.invoke(version: String, packageName: String) = Action (
    name = name,
    packageName = packageName,
    id = generateID(packageName, name),
    version = version,
    resultType = resultType,
    parameters = parameters,
    script = Script(script)
)