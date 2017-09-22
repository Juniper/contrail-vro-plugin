/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.vro.ContrailPluginFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList


interface PluginFactoryRepository {
    fun register(factory: ContrailPluginFactory)
    fun unregister(factory: ContrailPluginFactory)
}


interface PluginNotifications {
    fun notifyElementsInvalidate()
}


@Component
class PluginFactoryRepositoryImpl: PluginFactoryRepository, PluginNotifications {
    private val factories = CopyOnWriteArrayList<ContrailPluginFactory>()

    override fun register(factory: ContrailPluginFactory) {
        factories.add(factory)
    }

    override fun unregister(factory: ContrailPluginFactory) {
        factories.remove(factory)
    }

    override fun notifyElementsInvalidate() {
        factories.forEach { it.pluginNotificationHandler.notifyElementInvalidate(null, null) }
    }
}