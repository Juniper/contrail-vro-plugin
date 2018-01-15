/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import net.juniper.contrail.vro.model.ConnectionInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

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
: ConnectionPersister {
    companion object {
        @JvmStatic private val log = LoggerFactory.getLogger(PersisterUsingEndpointConfigurationService::class.java)
    }

    override fun findAll(): List<ConnectionInfo> {
        log.debug("Loading connections.")
        return configurationService.endpointConfigurations.asSequence()
            .map { it.asInfo }
            .filterNotNull()
            .onEach { log.trace("---> Loading connection info: {}", it) }
            .toList()
    }

    override fun save(connectionInfo: ConnectionInfo) {
        log.debug("Saving connection: {}", connectionInfo)
        createOrGetConfiguration(connectionInfo.name)
            .updateFrom(connectionInfo)
            .saveTo(configurationService)
    }

    override fun delete(connectionInfo: ConnectionInfo) {
        log.debug("Deleting connection: {}", connectionInfo)
        configurationService.deleteEndpointConfiguration(connectionInfo.name)
    }

    private fun createOrGetConfiguration(id: String): IEndpointConfiguration =
        configurationService.getEndpointConfiguration(id) ?:
            configurationService.newEndpointConfiguration(id)

    private fun IEndpointConfiguration.saveTo(service: IEndpointConfigurationService) {
        service.saveEndpointConfiguration(this)
    }

    private fun IEndpointConfiguration.updateFrom(info: ConnectionInfo): IEndpointConfiguration {
        setString(NAME, info.name)
        setString(HOST, info.hostname)
        setInt(PORT, info.port)
        setString(USER, info.username)
        setPassword(PASSWORD, info.password)
        setString(AUTHSERVER, info.authServer)
        setString(TENANT, info.tenant)

        return this
    }

    private val IEndpointConfiguration.asInfo: ConnectionInfo? get() {
        val name = getString(NAME) ?: return null
        val host = getString(HOST) ?: return null
        val port = getAsInteger(PORT) ?: return null
        val username = getString(USER)
        val password = getPassword(PASSWORD)
        val authServer = getString(AUTHSERVER)
        val tenant = getString(TENANT)

        return ConnectionInfo(name, host, port, username, password, authServer, tenant)
    }
}

