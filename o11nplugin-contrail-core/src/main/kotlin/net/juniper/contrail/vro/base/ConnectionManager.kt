/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.types.Domain
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionException
import net.juniper.contrail.vro.model.ConnectionInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Description("Object responsible for creating and deleting connections to Contrail controller.")
class ConnectionManager
@Autowired constructor(
    private val repository: ConnectionRepository,
    private val connectorFactory: ConnectorFactory,
    private val notifier: GlobalPluginNotificationHandler) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionManager::class.java)
        private val blankPattern = "\\s+".toRegex()
    }

    init {
        log.info("ConnectionManager created.")
    }

    private fun String.clean() =
        trim().replace(blankPattern, " ")

    fun create(name: String, host: String, port: Int, user: String?, password: String?, authServer: String?, tenant: String?): Connection {
        val info = ConnectionInfo(name.clean(), host, port, user?.trim(), password, authServer?.trim(), tenant?.trim())
        val connector = createConnector(info)
        val connection = Connection(info, connector)
        repository.addConnection(connection)
        notifier.notifyElementsInvalidate()
        return connection
    }

    private fun createConnector(info: ConnectionInfo): ApiConnector =
        connectorFactory.create(info).also { validate(it) }

    @Throws(ConnectionException::class)
    private fun validate(connector: ApiConnector) {
        try {
            // Try to list domains to check if connector is properly configured.
            // If connector is invalid exception will be thrown.
            val list = connector.list(Domain::class.java, null)
            // If list is null it means that user was not authorized to make request.
            if (list == null)
                throw ConnectionException("User not authorized to connect to Contrail controller.")
        } catch (ex: IOException) {
            throw ConnectionException("Could not connect to Contrail controller: ${ex.message}.")
        }
    }

    fun delete(connection: Connection) {
        repository.removeConnection(connection)
        notifier.notifyElementsInvalidate()
        connection.dispose()
    }

    fun connection(id: String?): Connection? {
        if (id == null) return null
        return repository.getConnection(Sid.valueOf(id))
    }

    val connections: List<Connection> get() =
        repository.connections
}
