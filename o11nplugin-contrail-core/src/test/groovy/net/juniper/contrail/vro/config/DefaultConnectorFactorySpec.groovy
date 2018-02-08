/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

class DefaultConnectorFactorySpec extends Specification {

    def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret", "http://localhost:5000")
    def infoNullServer = new ConnectionInfo("connection name", "host", 8080, "user", "secret", null)
    def connector = Mock(ApiConnector)
    def connectorSource = Mock(ConnectorSource)
    def factory = new DefaultConnectorFactory(connectorSource)

    def setup() {
        connectorSource.build(_, _) >> connector
    }

    def "Factory configures connector with credentials from ConnectionInfo"() {
        when:
        factory.create(info)

        then:
        1 * connector.credentials(info.username, info.password)
    }

    def "Factory sets authentication type to keystone when server is specified" () {
        when:
        factory.create(info)

        then:
        1 * connector.authServer("keystone", info.authServer)
    }


    def "Factory sets authentication type to null when server is null" () {
        when:
        factory.create(infoNullServer)

        then:
        (0..1) * connector.authServer(null, null)
        0 * connector.authServer(_, _)
    }


    def "Factory sets tenant name from ConnectionInfo" () {
        when:
        factory.create(info)

        then:
        1 * connector.tenantName(info.tenant)
    }
}
