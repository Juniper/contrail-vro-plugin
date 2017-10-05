/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import net.juniper.contrail.vro.model.ConnectionInfo
import spock.lang.Specification

import static net.juniper.contrail.vro.config.ConstantsKt.*

class PersisterUsingEndpointConfigurationServiceSpec extends Specification {
    def service = Mock(IEndpointConfigurationService)
    def persister = new PersisterUsingEndpointConfigurationService(service)
    def info = new ConnectionInfo("host", 8080, "user", "secret")

    def "Persister returns connections based on the configuration in the service" () {
        setup:
        def config = Mock(IEndpointConfiguration)

        config.getString(ID) >> info.id
        config.getString(HOST) >> info.hostname
        config.getAsInteger(PORT) >> info.port
        config.getString(USER) >> info.username
        config.getPassword(PASSWORD) >> info.password

        service.endpointConfigurations >> [config]

        when:
        def connections = persister.findAll()

        then:
        connections.size() == 1
        connections[0] == info
    }

    def "Persister adds connection to the service" () {
        given:
        def config = Mock(IEndpointConfiguration)
        service.getEndpointConfiguration(info.id) >> config

        when:
        persister.save(info)

        then:
        1 * service.saveEndpointConfiguration(config)
        1 * config.setString  (ID,           info.id)
        1 * config.setString  (HOST,         info.hostname)
        1 * config.setInt     (PORT,         info.port)
        1 * config.setString  (USER,         info.username)
        1 * config.setPassword(PASSWORD ,    info.password)
        1 * config.setString  (TENANT,       info.tenant)
        1 * config.setString  (AUTHSERVER,   info.authServer)
    }

    def "Persister deletes connection from the service" () {
        when:
        persister.delete(info)

        then:
        1 * service.deleteEndpointConfiguration(info.id)
    }
}
