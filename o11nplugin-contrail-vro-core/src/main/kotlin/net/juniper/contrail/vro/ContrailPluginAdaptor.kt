/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import javax.security.auth.login.LoginException
import net.juniper.contrail.vro.config.PluginFactoryRepository
import net.juniper.contrail.vro.config.SpringConfig
import net.juniper.contrail.vro.config.ConnectionManager
import org.slf4j.LoggerFactory
import ch.dunes.vso.sdk.IServiceRegistry
import ch.dunes.vso.sdk.IServiceRegistryAdaptor
import ch.dunes.vso.sdk.api.IPluginAdaptor
import ch.dunes.vso.sdk.api.IPluginEventPublisher
import ch.dunes.vso.sdk.api.IPluginFactory
import ch.dunes.vso.sdk.api.IPluginNotificationHandler
import ch.dunes.vso.sdk.api.IPluginPublisher
import ch.dunes.vso.sdk.api.PluginLicense
import ch.dunes.vso.sdk.api.PluginLicenseException
import ch.dunes.vso.sdk.api.PluginWatcher
import com.vmware.o11n.plugin.sdk.spring.impl.PluginNameAwareBeanPostProcessor
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.GenericApplicationContext

class ContrailPluginAdaptor(private val context: GenericApplicationContext) : IPluginAdaptor, IServiceRegistryAdaptor {

    private val log = LoggerFactory.getLogger(ContrailPluginAdaptor::class.java)

    private var pluginName: String? = null

    internal constructor() : this(AnnotationConfigApplicationContext(SpringConfig::class.java))

    @Throws(SecurityException::class, LoginException::class, PluginLicenseException::class)
    override fun createPluginFactory(sessionId: String, username: String, password: String, pluginNotificationHandler: IPluginNotificationHandler): IPluginFactory {
        log.debug("createPluginFactory() --> sessionId: $sessionId, username: $username")
        val connections = context.getBean(ConnectionManager::class.java)
        val factory = ContrailPluginFactory(connections, pluginNotificationHandler)
        factoryRepository().register(factory)
        return factory
    }

    private fun factoryRepository(): PluginFactoryRepository {
        return context.getBean(PluginFactoryRepository::class.java)
    }

    private fun registerSingleton(name: String, bean: Any) {
        context.defaultListableBeanFactory.registerSingleton(name, bean)
    }

    override fun setServiceRegistry(serviceRegistry: IServiceRegistry) {
        log.debug("Setting service registry.")

        registerSingleton("serviceRegistry", serviceRegistry)

        val endpointService = serviceRegistry.getService("ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService")
        if (endpointService != null) {
            registerSingleton("endpointConfigurationService", endpointService)
        } else {
            log.error("Endpoint configuration service not found in the service registry.")
        }
    }

    override fun setPluginName(pluginName: String) {
        log.debug("Setting plugin name: " + pluginName)
        this.pluginName = pluginName
        registerSingleton("pluginName", pluginName)
        context.defaultListableBeanFactory
            .addBeanPostProcessor(PluginNameAwareBeanPostProcessor(pluginName))
    }

    override fun setPluginPublisher(pluginPublisher: IPluginPublisher) {
        log.debug("Setting plugin publisher.")
        registerSingleton("pluginPublisher", pluginPublisher)
    }

    override fun addWatcher(pluginWatcher: PluginWatcher) {
        log.debug("addWatcher() --> pluginWatcher: " + pluginWatcher.id)
    }

    override fun removeWatcher(pluginWatcherId: String) {
        log.debug("removeWatcher() --> pluginWatcherId: " + pluginWatcherId)
    }

    override fun registerEventPublisher(type: String, id: String, pluginEventPublisher: IPluginEventPublisher) {
        log.debug("registerEventPublisher() --> type: $type, id: $id")
    }

    override fun unregisterEventPublisher(type: String, id: String, pluginEventPublisher: IPluginEventPublisher) {
        log.debug("unregisterEventPublisher() --> type: $type, id: $id")
    }

    @Throws(PluginLicenseException::class)
    override fun installLicenses(licenses: Array<PluginLicense>) {
        log.debug("installLicenses()")
    }

    override fun uninstallPluginFactory(pluginFactory: IPluginFactory) {
        if (pluginFactory is ContrailPluginFactory) {
            log.debug("Uninstalling factory.")
            factoryRepository().unregister(pluginFactory)
        } else {
            log.error("Uninstall plugin factory called on unknown factory type.")
        }
    }
}
