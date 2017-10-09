/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
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
        log.info("ConnectionInfoManager created.")
    }

    private fun String.clean() =
        trim().replace(blankPattern, " ")

    fun create(name: String, host: String, port: Int, user: String?, password: String?, authServer: String?, tenant: String?): String {
        val info = ConnectionInfo(name.clean(), host, port, user, password, authServer, tenant)
        val connector = connectorFactory.create(info)
        val connection = Connection(info, connector)
        repository.addConnection(connection)
        notifier.notifyElementsInvalidate()
        return connection.name
    }

    fun delete(connection: Connection) {
        repository.removeConnection(connection)
        notifier.notifyElementsInvalidate()
    }
}
