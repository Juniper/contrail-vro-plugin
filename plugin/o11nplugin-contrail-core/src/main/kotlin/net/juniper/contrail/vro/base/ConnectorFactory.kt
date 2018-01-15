/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import com.google.common.annotations.VisibleForTesting
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.ApiConnectorFactory
import net.juniper.contrail.vro.model.ConnectionInfo
import org.springframework.stereotype.Component

/**
 * Interface for creating Contrail API interface
 * based on the connection information.
 */
interface ConnectorFactory {
    fun create(info: ConnectionInfo): ApiConnector
}

/**
 * Internal interface to allow testing.
 */
@VisibleForTesting
interface ConnectorSource {
    fun build(hostname: String, port: Int): ApiConnector
}

private object DefaultConnectorSource : ConnectorSource {
    override fun build(hostname: String, port: Int): ApiConnector =
        ApiConnectorFactory.build(hostname, port)
}

/**
 * Default implementation of the [ConnectorFactory]
 * that delegates to Contrail provided factory for
 * instantiation of the API interface.
 */
@Component
class DefaultConnectorFactory(private val source: ConnectorSource) : ConnectorFactory {
    constructor(): this(DefaultConnectorSource)

    override fun create(info: ConnectionInfo): ApiConnector {
        val authType = if (info.authServer != null ) AUTHTYPE else null
        return source.build(info.hostname, info.port).apply {
            credentials(info.username, info.password)
            authServer(authType, info.authServer)
            tenantName(info.tenant)
        }
    }
}
