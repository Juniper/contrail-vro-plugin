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
import java.util.*


/**
 * Service responsible for persistent storage of Contrail
 * connection information. Required to maintain plugin state
 * when plugin is not running.
 */
interface ConnectionPersister {
    fun findAll(): List<ConnectionInfo>
    fun save(connectionInfo: ConnectionInfo)
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
    private val configurationService: IEndpointConfigurationService
)
: ConnectionPersister
{
    private val log = LoggerFactory.getLogger(PersisterUsingEndpointConfigurationService::class.java)

    override fun findAll(): List<ConnectionInfo> =
        configurationService.endpointConfigurations.asSequence()
            .map { it.asInfo() }
            .onEach { log.debug("Loading connection info: " + it) }
            .toList()

    override fun save(connectionInfo: ConnectionInfo) =
        createOrGetConfiguration(connectionInfo.id)
            .updateFrom(connectionInfo)
            .saveTo(configurationService)

    override fun delete(connectionInfo: ConnectionInfo) =
        configurationService.deleteEndpointConfiguration(connectionInfo.id)

    private fun createOrGetConfiguration(id: String):IEndpointConfiguration =
        configurationService.getEndpointConfiguration(id) ?:
            configurationService.newEndpointConfiguration(id)

    private fun IEndpointConfiguration.saveTo(service: IEndpointConfigurationService) {
        service.saveEndpointConfiguration(this)
    }

    private fun IEndpointConfiguration.updateFrom(info: ConnectionInfo): IEndpointConfiguration {
        setString(ID, info.id)
        setString(HOST, info.hostname)
        setInt(PORT, info.port)
        setString(USER, info.username)
        setPassword(PASSWORD, info.password)

        return this
    }

    private fun IEndpointConfiguration.asInfo(): ConnectionInfo {
        val id = UUID.fromString(getString(ID))
        val host = getString(HOST)
        val port = getAsInteger(PORT)
        val username = getString(USER)
        val password = getPassword(PASSWORD)

        return ConnectionInfo(id, host, port, username, password)
    }
}

