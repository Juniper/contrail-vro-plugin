/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.spring

import ch.dunes.vso.sdk.IServiceRegistry
import ch.dunes.vso.sdk.api.IPluginNotificationHandler
import ch.dunes.vso.sdk.api.IPluginPublisher
import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.config.ConnectionManager
import net.juniper.contrail.vro.config.DefaultConnectionRepository
import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Shared
import spock.lang.Specification

import static net.juniper.contrail.vro.config.ConstantsKt.*

class SpringContextSpec extends Specification {
    static def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
    def connection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))
    @Shared
    def pluginFactory
    @Shared
    def manager
    @Shared
    def repository
    @Shared
    def notifier

    def setupSpec() {
        def pluginAdaptor = new ContrailPluginAdaptor()
        def pluginPublisher = Mock(IPluginPublisher)
        def configService = mockConfigurationService()
        def registry = mockServiceRegistry(configService)
        pluginAdaptor.setServiceRegistry(registry)
        pluginAdaptor.setPluginPublisher(pluginPublisher)

        def context = pluginAdaptor.getApplicationContext()
        manager = context.getBean(ConnectionManager)
        repository = context.getBean(DefaultConnectionRepository)
        notifier = context.getBean(GlobalPluginNotificationHandler)
    }

    def mockServiceRegistry(configurationService) {
        def serviceRegistry = Mock(IServiceRegistry)
        serviceRegistry.getService("ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService") >> configurationService

        return serviceRegistry
    }

    def mockConfigurationService() {
        def configurationService = Mock(IEndpointConfigurationService)
        def config = Mock(IEndpointConfiguration)

        config.getString(NAME) >> "other name"
        config.getString(HOST) >> info.hostname
        config.getAsInteger(PORT) >> info.port
        config.getString(USER) >> info.username
        config.getPassword(PASSWORD) >> info.password

        configurationService.getEndpointConfiguration(info.name) >> config
        configurationService.endpointConfigurations >> [config]
        configurationService.newEndpointConfiguration(_) >> config

        return configurationService
    }

    def "Calling create inserts connection into repository"() {
        when:
        def old_size = repository.connections.size()
        def name = manager.create(info.name, info.hostname, info.port, info.username, info.password, info.tenant, info.authServer)

        then:
        old_size + 1 == repository.connections.size()
        info == repository.getConnection(Sid.valueOf(info.name)).info
    }

    def "Calling deleted removes connection from repository"() {
        when:
        def old_size = repository.connections.size()
        manager.delete(connection)

        then:
        old_size - 1 == repository.connections.size()

    }
}

