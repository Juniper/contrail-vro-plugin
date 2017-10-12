/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.vro.model.Connection
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.ArrayList
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
    fun getConnection(name: String): Connection?

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
    companion object {
        private val log = LoggerFactory.getLogger(DefaultConnectionRepository::class.java)
    }

    private val items = ConcurrentHashMap<String, Connection>()

    private fun String.toKey() =
        toLowerCase()

    private val Connection.key: String get() =
        info.name.toKey()

    @Throws(IllegalArgumentException::class)
    override fun addConnection(item: Connection) {

        val key = item.key
        if (items.containsKey(key)) {
            throw IllegalArgumentException("Item with id '$key' already exists!")
        }

        items.put(key, item)

        persister.save(item.info)
    }

    @Throws(IllegalArgumentException::class)
    override fun removeConnection(item: Connection) {
        val connection = items.remove(item.key)
        if (connection != null)
            persister.delete(connection.info)
    }

    override val connections: List<Connection>
        get() = ArrayList(items.values)

    @Throws(IllegalArgumentException::class)
    override fun getConnection(name: String): Connection? {

        if (StringUtils.isBlank(name)) {
            throw IllegalArgumentException("'name' is empty")
        }

        return items[name.toKey()]
    }

    override fun findConnections(query: String): List<Connection> {
        val cleanedQuery = query.trim()
        return items.values.asSequence()
            .filter { it.key.startsWith(cleanedQuery, ignoreCase = true) }
            .toList()
    }
}
