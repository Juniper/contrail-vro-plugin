/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.spring

import ch.dunes.vso.sdk.IServiceRegistry
import ch.dunes.vso.sdk.api.IPluginPublisher
import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.base.ConnectionManager
import net.juniper.contrail.vro.base.ConnectorFactory
import net.juniper.contrail.vro.base.DefaultConnectionRepository
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionException
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Shared
import spock.lang.Specification

import static net.juniper.contrail.vro.base.ConstantsKt.*

class SpringContextSpec extends Specification {
    static def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
    def connection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))
    @Shared
    def manager
    @Shared
    def mockedManager
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

        def apiConnector = Mock(ApiConnector)
        apiConnector.list(_, _) >> new ArrayList<ApiObjectBase>()
        def connectorFactory = Mock(ConnectorFactory)
        connectorFactory.create(_) >> apiConnector
        mockedManager = new ConnectionManager(repository, connectorFactory, notifier)
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
        given:
        def old_size = repository.connections.size()

        when:
        def newConnection = mockedManager.create(info.name, info.hostname, info.port, info.username, info.password, info.tenant, info.authServer)

        then:
        old_size + 1 == repository.connections.size()
        info == repository.getConnection(Sid.valueOf(info.name)).info
    }

    def "Calling create with invalid credentials throws ConnectionException"() {
        when:
        manager.create(info.name, info.hostname, info.port, info.username, info.password, info.tenant, info.authServer)

        then:
        thrown ConnectionException
    }

    def "Calling deleted removes connection from repository"() {
        when:
        def old_size = repository.connections.size()
        manager.delete(connection)

        then:
        old_size - 1 == repository.connections.size()
    }
}

