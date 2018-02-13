/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.propertyNotNull
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.ofType
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.generateID

private val item = "item"
private val parameterPath = "parameterPath"

internal fun propertyRetrievalAction(version: String, packageName: String): Action {
    val name = propertyValue

    val parameters = listOf(
        item ofType any,
        parameterPath ofType string
    )

    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = any,
        parameters = parameters,
        script = Script(retrievalActionScript)
    )
}

internal fun propertyNotNullAction(version: String, packageName: String): Action {
    val name = propertyNotNull

    val parameters = listOf(
        item ofType any,
        parameterPath ofType string
    )

    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = boolean,
        parameters = parameters,
        script = Script(notNullActionScript)
    )
}

val retrievalActionScript = """
if(!item) return null;
return eval('$item.' + $parameterPath);
""".trim()

val notNullActionScript = """
if(!item) return false;
return eval('$item.' + $parameterPath) != null;
""".trim()

