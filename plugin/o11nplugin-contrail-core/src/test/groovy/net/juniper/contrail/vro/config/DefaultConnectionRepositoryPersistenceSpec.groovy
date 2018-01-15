/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import net.juniper.contrail.api.ApiConnectorMock
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

class DefaultConnectionRepositoryPersistenceSpec extends Specification {

    def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
    def connection = new Connection(info, new ApiConnectorMock(info.hostname, info.port))
    def persister = Mock(ConnectionPersister)
    def repository = new DefaultConnectionRepository(persister)


    def "Repository notifies the persister when connection is added"() {
        when:
        repository.addConnection(connection)

        then:
        1 * persister.save(info)
    }

    def "Repository notifies the persister when connection is removed"() {
        given:
        repository.addConnection(connection)

        when:
        repository.removeConnection(connection)

        then:
        1 * persister.delete(info)
    }
}
