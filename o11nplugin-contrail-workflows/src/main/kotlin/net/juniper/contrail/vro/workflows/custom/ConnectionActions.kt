/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.parentConnection
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.ofType
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.util.generateID

internal fun parentConnectionAction(version: String, packageName: String): Action {
    val name = parentConnection
    val resultType = Connection.reference
    val parameters = listOf(item ofType any)
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(parentConnectionScript)
    )
}

private val parentConnectionScript = """
var id = item.internalId;
var connection = ContrailConnectionManager.connection(id.toString());
connection.internalId = id;
return connection;
"""