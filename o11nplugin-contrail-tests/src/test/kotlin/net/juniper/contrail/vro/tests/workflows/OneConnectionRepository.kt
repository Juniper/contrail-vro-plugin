/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.model.Connection

class OneConnectionRepository(private val connection: Connection) : ConnectionRepository {
    override fun addConnection(item: Connection) = Unit

    override fun removeConnection(item: Connection) = Unit

    override fun getConnection(id: Sid): Connection? = connection

    override fun findConnections(query: String): List<Connection> = listOf(connection)

    override val connections: List<Connection> = listOf(connection)
}