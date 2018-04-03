/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

class ConnectionManagerSpec extends Specification {

    def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
    def connection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))
    def repository = Mock(ConnectionRepository)
    def factory = Mock(ConnectorFactory)
    def notifier = Mock(GlobalPluginNotificationHandler)
    def manager = new ConnectionManager(repository, factory, notifier)

    def setup() {
        def connector = Mock(ApiConnector)
        factory.create(_) >> connector
        connector.list(_, _) >> new ArrayList<ApiObjectBase>()
    }

    def "Calling create inserts connection into repository and notifies plugin" () {
        when:
        manager.create(info.name, info.hostname, info.port, info.username, info.password, info.tenant, info.authServer)

        then:
        1 * repository.addConnection(_)
        1 * notifier.notifyElementsInvalidate()
    }

    def "Create trims name and removes duplicate whitespace"() {
        given:
        def nameWithDuplicatedSpaces = info.name.replace(" ", "  ")
        def dirtyName = " $nameWithDuplicatedSpaces "

        when:
        def newConnection = manager.create(dirtyName, info.hostname, info.port, info.username, info.password, info.tenant, info.authServer)

        then:
        newConnection.name == info.name
    }

    def "Calling connection with existing id retrieves it from repository" () {
        given:
        repository.getConnection(info.sid) >> connection

        when:
        def retrieved = manager.connection(info.sid.toString())

        then:
        retrieved == connection
    }

    def "Calling connection with invalid id returns null" () {
        given:
        repository.getConnection(info.sid) >> connection

        when:
        def retrieved = manager.connection("invalid id")

        then:
        retrieved == null
    }

    def "Calling deleted removes connection from repository and notifies plugin" () {
        when:
        manager.delete(connection)

        then:
        1 * repository.removeConnection(connection)
        1 * notifier.notifyElementsInvalidate()
    }
}
