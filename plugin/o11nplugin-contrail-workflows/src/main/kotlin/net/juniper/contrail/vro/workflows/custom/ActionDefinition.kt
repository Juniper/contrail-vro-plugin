/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.cidrCheckingAction
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.ActionParameter
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.generateID

internal fun addCidrValidationAction(version: String, packageName: String): Action {
    val name = cidrCheckingAction
    val resultType = string
    val parameters = listOf(ActionParameter("input", string))
    return Action(
            name = name,
            packageName = packageName,
            id = generateID(packageName, name),
            version = version,
            resultType = resultType,
            parameters = parameters,
            script = Script(ScriptLoader.load(name))
    )
}