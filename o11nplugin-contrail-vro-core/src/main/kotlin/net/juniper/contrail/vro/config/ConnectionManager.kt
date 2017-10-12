/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import ch.dunes.vso.sdk.api.IPluginFactory
import net.juniper.contrail.vro.ContrailPluginFactory
import net.juniper.contrail.vro.model.ConnectionInfo
import net.juniper.contrail.vro.model.Connection
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ConnectionManager
@Autowired constructor(
    private val repository: ConnectionRepository,
    private val connectorFactory: ConnectorFactory,
    private val notifier: PluginNotifications) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionManager::class.java)
        private val blankPattern = "\\s+".toRegex()

        /**
         * Method retrieves an instance of [ConnectionManager] from [ContrailPluginFactory].
         *
         * This method is never called directly and is only required by the vRO
         * so that [ConnectionManager] could be accessed as a singleton from JavaScript.
         */
        @JvmStatic fun createScriptingSingleton(factory: IPluginFactory): ConnectionManager =
            (factory as ContrailPluginFactory).connections
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
