/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

class DefaultConnectionRepositorySpec extends Specification {

    def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
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
        def returnedConnection = repository.getConnection(info.sid)

        then:
        returnedConnection == null
    }

    def "Added connection is returned by id" () {
        given:
        repository.addConnection(connection)

        when:
        def returnedConnection = repository.getConnection(info.sid)

        then:
        returnedConnection == connection
    }

    def "Cannot add connection with the same name"() {
        given:
        repository.addConnection(connection)

        when:
        repository.addConnection(connection)

        then:
        thrown IllegalArgumentException
    }

    def "Cannot add connection with the same name - case sensitive"() {
        given:
        repository.addConnection(connection)
        info = new ConnectionInfo("CONNECTION NAME", "host", 8080, "user", "secret")
        def newConnection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))

        when:
        repository.addConnection(newConnection)

        then:
        thrown IllegalArgumentException
    }

    def "Added connection is returned in connection list" () {
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
        def returnedConnection = repository.getConnection(info.sid)

        then:
        returnedConnection == null
    }
}
