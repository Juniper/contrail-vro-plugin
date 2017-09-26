/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

class DefaultConnectionRepositoryOperationSpec extends Specification {

    def info = new ConnectionInfo("host", 8080, "user", "secret")
    def connection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))
    def persister = Mock(ConnectionPersister)
    def repository = new DefaultConnectionRepository(persister)

    def "Initially repository returns empty connection list" () {
        when:
        def connections = repository.connections

        then:
        connections.size() == 0
    }

    def "Repository returns null for unknown connection Id" () {

        when:
        def returnedConnection = repository.getConnection(connection.id)

        then:
        returnedConnection == null
    }

    def "Added connection is returned by id" () {
        given:
        repository.addConnection(connection)

        when:
        def returnedConnection = repository.getConnection(connection.id)

        then:
        returnedConnection == connection
    }

    def "Added repository is returned in connection list" () {
        given:
        repository.addConnection(connection)

        when:
        def connections = repository.connections

        then:
        connections.size() == 1
        connections.get(0) == connection
    }

    def "Deleted connection is not available in the repository" () {
        given:
        repository.addConnection(connection)

        when:
        repository.removeConnection(connection)

        and:
        def returnedConnection = repository.getConnection(connection.id)

        then:
        returnedConnection == null
    }
}
