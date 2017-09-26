/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import ch.dunes.vso.sdk.api.IPluginNotificationHandler
import net.juniper.contrail.vro.ContrailPluginFactory
import spock.lang.Specification

class NotifyingPluginFactoryRepositorySpec extends Specification {

    def notificationHandler = Mock(IPluginNotificationHandler)
    def factory = Mock(ContrailPluginFactory)
    def repository = new NotifyingPluginFactoryRepository()

    def "Initially repository does not contain any factory" () {
        when: "repository is created"

        then:
        !repository.contains(factory)
    }

    def "Repository contains added factory" () {
        when:
        repository.register(factory)

        then:
        repository.contains(factory)
    }

    def "Repository does not contain removed factory" () {
        when:
        repository.register(factory)

        and:
        repository.unregister(factory)

        then:
        !repository.contains(factory)
    }

    def "Notification handler is notified when repository is notified" () {
        given:
        factory.pluginNotificationHandler >> notificationHandler
        repository.register(factory)

        when:
        repository.notifyElementsInvalidate()

        then:
        1 * notificationHandler.notifyElementInvalidate(null, null)
    }
}
