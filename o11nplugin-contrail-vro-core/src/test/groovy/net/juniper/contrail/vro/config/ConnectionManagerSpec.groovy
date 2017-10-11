/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

class ConnectionManagerSpec extends Specification {

    def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
    def connector = new ApiConnectorMock(info.hostname, info.port)
    def connection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))
    def repository = Mock(ConnectionRepository)
    def factory = Mock(ConnectorFactory)
    def notifier = Mock(PluginNotifications)
    def manager = new ConnectionManager(repository, factory, notifier)

    def "Calling create inserts connection into repository and notifies plugin" () {
        given:
        factory.create(_) >> connector

        when:
        manager.create(info.name, info.hostname, info.port, info.username, info.password, info.tenant, info.authServer)

        then:
        1 * repository.addConnection(_)
        1 * notifier.notifyElementsInvalidate()
    }

    def "Calling deleted removes connection from repository and notifies plugin" () {
        when:
        manager.delete(connection)

        then:
        1 * repository.removeConnection(connection)
        1 * notifier.notifyElementsInvalidate()
    }
}
