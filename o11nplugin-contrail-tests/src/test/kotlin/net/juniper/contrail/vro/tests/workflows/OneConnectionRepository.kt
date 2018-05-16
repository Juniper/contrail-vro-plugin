package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.model.Connection

class OneConnectionRepository(private val connection: Connection) : ConnectionRepository {
    override fun addConnection(item: Connection) {
    }

    override fun removeConnection(item: Connection) {
    }

    override fun getConnection(id: Sid): Connection? {
        return connection
    }

    override fun findConnections(query: String): List<Connection> {
        return listOf(connection)
    }

    override val connections: List<Connection> = listOf(connection)
}