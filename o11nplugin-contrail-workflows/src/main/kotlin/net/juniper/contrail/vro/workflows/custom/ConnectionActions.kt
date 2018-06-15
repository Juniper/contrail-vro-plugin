/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.parentConnection
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.reference

val parentConnectionAction = ActionDefinition (
    name = parentConnection,
    resultType = Connection.reference,
    parameters = listOf(item ofType any)
)

val defaultConnection = ActionDefinition (
    name = defaultConnection,
    resultType = Connection.reference,
    parameters = listOf()
)