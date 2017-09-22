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
    fun removeConnection(key: String): Connection?

    @Throws(IllegalArgumentException::class)
    fun getConnection(key: String): Connection?

    val connections: List<Connection>
}


@Lazy @Component
class ConnectionRepositoryImpl
@Lazy @Autowired constructor
(
    private val persister: ConnectionPersister
)
: ConnectionRepository
{
    private val items = ConcurrentHashMap<String, Connection>()

    init {
        log.info("ConnectionInfoRepository created.")
    }

    private fun getKey(info: Connection): String =
        info.id

    @Throws(IllegalArgumentException::class)
    override fun addConnection(item: Connection) {

        val key = getKey(item)
        if (items.containsKey(key)) {
            throw IllegalArgumentException("Item with id '$key' already exists!")
        }

        items.put(key, item)

        persister.save(item.info)
    }

    @Throws(IllegalArgumentException::class)
    fun updateConnection(key: String, item: Connection): Connection? {

        if (!items.containsKey(key)) {
            return null
        }

        addConnection(item)

        return item
    }

    @Throws(IllegalArgumentException::class)
    override fun removeConnection(key: String): Connection? {
        val connection = items.remove(key)
        if (connection != null)
            persister.delete(connection.info)
        return connection
    }

    override val connections: List<Connection>
        get() = ArrayList(items.values)

    @Throws(IllegalArgumentException::class)
    override fun getConnection(key: String): Connection? {

        if (StringUtils.isBlank(key)) {
            throw IllegalArgumentException("'key' is empty")
        }

        return items[key]
    }

    companion object {

        private val log = LoggerFactory.getLogger(ConnectionRepositoryImpl::class.java)
    }
}
