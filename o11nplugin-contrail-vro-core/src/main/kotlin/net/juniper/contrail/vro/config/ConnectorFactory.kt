/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

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
 * Default implementation of the ConnectorFactory
 * that delegates to Contrail provided factory for
 * instantiation of the API interface.
 */
@Component
class ConnectorFactoryImpl : ConnectorFactory {
    override fun create(info: ConnectionInfo): ApiConnector {
        val authType = if (info.authServer != null ) AUTHTYPE else null
        return ApiConnectorFactory.build(info.hostname, info.port)
            .credentials(info.hostname, info.password)
            .tenantName(info.tenant)
            .authServer(authType, info.authServer)
    }
}
