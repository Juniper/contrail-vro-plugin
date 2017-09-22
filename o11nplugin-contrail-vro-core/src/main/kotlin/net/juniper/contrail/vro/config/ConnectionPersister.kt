/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import net.juniper.contrail.vro.model.ConnectionInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*


/**
 * Service responsible for persistent storage of Contrail
 * connection information. Required to maintain plugin state
 * when plugin is not running.
 */
interface ConnectionPersister {
    fun findAll(): List<ConnectionInfo>

    fun save(connectionInfo: ConnectionInfo): ConnectionInfo

    fun delete(connectionInfo: ConnectionInfo)
}


/**
 * Persister implementation using VRO provided service.
 */
@Lazy @Component
@Profile("default")
class PersisterUsingEndpointConfigurationService
@Lazy @Autowired constructor
(
    private val endpointConfigurationService: IEndpointConfigurationService
)
: ConnectionPersister
{

    override fun findAll(): List<ConnectionInfo> {
        try {
            val configs = endpointConfigurationService.endpointConfigurations
            val result = ArrayList<ConnectionInfo>(configs.size)

            for (config in configs) {
                val connectionInfo = getConnectionInfo(config)
                if (connectionInfo != null) {
                    log.debug("Adding connection info to result map: " + connectionInfo)
                    result.add(connectionInfo)
                }
            }
            return result
        } catch (e: IOException) {
            log.debug("Error reading connections.", e)
            throw RuntimeException(e)
        }

    }

    override fun save(connectionInfo: ConnectionInfo): ConnectionInfo {
        try {
            var endpointConfiguration: IEndpointConfiguration? = endpointConfigurationService
                .getEndpointConfiguration(connectionInfo.id.toString())

            if (endpointConfiguration == null) {
                endpointConfiguration = endpointConfigurationService
                    .newEndpointConfiguration(connectionInfo.id.toString())
            }

            addConnectionInfoToConfig(endpointConfiguration!!, connectionInfo)

            endpointConfigurationService.saveEndpointConfiguration(endpointConfiguration)

            return connectionInfo
        } catch (e: IOException) {
            log.error("Error saving connection " + connectionInfo, e)
            throw RuntimeException(e)
        }

    }

    override fun delete(connectionInfo: ConnectionInfo) {
        try {

            endpointConfigurationService.deleteEndpointConfiguration(connectionInfo.id.toString())

        } catch (e: IOException) {
            log.error("Error deleting endpoint configuration: " + connectionInfo, e)
            throw RuntimeException(e)
        }

    }

    private fun addConnectionInfoToConfig(config: IEndpointConfiguration, info: ConnectionInfo) {
        try {
            config.setString(ID, info.id.toString())
            config.setString(HOST, info.hostname)
            config.setInt(PORT, info.port)
            config.setString(USER, info.username)
            config.setPassword(PASSWORD, info.password)

        } catch (e: Exception) {
            log.error("Error converting ConnectionInfo to IEndpointConfiguration.", e)
            throw RuntimeException(e)
        }

    }

    private fun getConnectionInfo(config: IEndpointConfiguration): ConnectionInfo? {
        try {
            val id = UUID.fromString(config.getString(ID))
            val host = config.getString(HOST)
            val port = config.getAsInteger(PORT)
            val username = config.getString(USER)
            val password = config.getPassword(PASSWORD)

            return ConnectionInfo(id, host, port, username, password)

        } catch (e: IllegalArgumentException) {
            log.warn("Cannot convert IEndpointConfiguration to ConnectionInfo: " + config.id, e)
            return null
        } catch (e: NullPointerException) {
            log.warn("Cannot convert IEndpointConfiguration to ConnectionInfo: " + config.id, e)
            return null
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(PersisterUsingEndpointConfigurationService::class.java)

        val ID = "id"
        val HOST = "host"
        val PORT = "port"
        val USER = "user"
        val PASSWORD = "password"
    }
}

