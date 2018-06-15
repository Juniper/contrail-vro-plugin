/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.vro.base.ConnectionManager
import net.juniper.contrail.vro.base.ConnectionPersister
import net.juniper.contrail.vro.base.ConnectorFactory
import net.juniper.contrail.vro.base.DefaultConnectionRepository
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo

import static net.juniper.contrail.vro.config.Actions.defaultConnection

class DefaultConnectionSpec extends ActionSpec {
    def defaultConnectionAction = actionFromScript(defaultConnection)

    def persister = Stub(ConnectionPersister)
    def notifier = Stub(GlobalPluginNotificationHandler)
    def connectorFactory = Stub(ConnectorFactory)
    def connector = Stub(ApiConnector)

    def connection1 = new Connection(new ConnectionInfo("c1", "1.2.3.4", 22), connector)
    def connection2 = new Connection(new ConnectionInfo("c2", "4.3.2.1", 33), connector)

    def repository = new DefaultConnectionRepository(persister)
    def connectionManager = new ConnectionManager(repository, connectorFactory, notifier)

    @Override
    def additionalSetup() {
        addToContext("ContrailConnectionManager", connectionManager)
    }

    def "Default connection is null when there are no connections"() {
        given:

        when:
        def result = engine.invokeFunction(defaultConnectionAction)

        then:
        result == null
    }
    def "Default connection is returned if only one connection is present in the repository"() {
        given:
        repository.addConnection(connection1)

        when:
        def result = engine.invokeFunction(defaultConnectionAction)

        then:
        result == connection1
    }
    def "Default connection is null when there is more than one connection"() {
        given:
        repository.addConnection(connection1)
        repository.addConnection(connection2)

        when:
        def result = engine.invokeFunction(defaultConnectionAction)

        then:
        result == null
    }
}
