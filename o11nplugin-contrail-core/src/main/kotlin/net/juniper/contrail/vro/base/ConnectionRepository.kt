/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.vro.model.Connection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Service holding all currently active Contrail connections.
 */
interface ConnectionRepository {

    @Throws(IllegalArgumentException::class)
    fun addConnection(item: Connection)

    @Throws(IllegalArgumentException::class)
    fun removeConnection(item: Connection)

    @Throws(IllegalArgumentException::class)
    fun getConnection(id: Sid): Connection?

    fun findConnections(query: String): List<Connection>

    val connections: List<Connection>
}

@Lazy @Component
class DefaultConnectionRepository
@Lazy @Autowired constructor
(
    private val persister: ConnectionPersister
)
: ConnectionRepository
{
    private val items = ConcurrentHashMap<String, Connection>()

    private fun Sid.toKey() =
        id.toString().toLowerCase()

    private val Connection.key: String get() =
        info.sid.toKey()

    @Throws(IllegalArgumentException::class)
    override fun addConnection(item: Connection) {
        val key = item.key
        if (items.containsKey(key)) {
            throw IllegalArgumentException("Item with id '$key' already exists!")
        }

        items[key] = item

        persister.save(item.info)
    }

    @Throws(IllegalArgumentException::class)
    override fun removeConnection(item: Connection) {
        val connection = items.remove(item.key)
        if (connection != null)
            persister.delete(connection.info)
    }

    override val connections: List<Connection> get() =
        items.values.toList()

    @Throws(IllegalArgumentException::class)
    override fun getConnection(id: Sid): Connection? =
        items[id.toKey()]

    override fun findConnections(query: String): List<Connection> {
        val cleanedQuery = query.trim()
        return items.values.asSequence()
            .filter { it.name.startsWith(cleanedQuery, ignoreCase = true) }
            .toList()
    }
}