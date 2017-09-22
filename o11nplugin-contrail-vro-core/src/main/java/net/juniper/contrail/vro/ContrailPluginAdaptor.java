/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import javax.security.auth.login.LoginException;

import ch.dunes.vso.sdk.IServiceRegistry;
import ch.dunes.vso.sdk.IServiceRegistryAdaptor;
import com.vmware.o11n.plugin.sdk.spring.impl.PluginNameAwareBeanPostProcessor;
import net.juniper.contrail.vro.config.PluginFactoryRepository;
import net.juniper.contrail.vro.config.SpringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.vso.sdk.api.IPluginAdaptor;
import ch.dunes.vso.sdk.api.IPluginEventPublisher;
import ch.dunes.vso.sdk.api.IPluginFactory;
import ch.dunes.vso.sdk.api.IPluginNotificationHandler;
import ch.dunes.vso.sdk.api.IPluginPublisher;
import ch.dunes.vso.sdk.api.PluginLicense;
import ch.dunes.vso.sdk.api.PluginLicenseException;
import ch.dunes.vso.sdk.api.PluginWatcher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class ContrailPluginAdaptor implements IPluginAdaptor, IServiceRegistryAdaptor {

    private static final Logger log = LoggerFactory.getLogger(ContrailPluginAdaptor.class);

    private String pluginName;

    private final GenericApplicationContext context;

    public ContrailPluginAdaptor(GenericApplicationContext context) {
        this.context = context;
    }

    ContrailPluginAdaptor() {
        this(new AnnotationConfigApplicationContext(SpringConfig.class));
    }

    @Override
    public IPluginFactory createPluginFactory(String sessionId, String username, String password, IPluginNotificationHandler pluginNotificationHandler) throws SecurityException, LoginException, PluginLicenseException {
        log.debug("createPluginFactory() --> sessionId: " + sessionId + ", username: " + username);
        ContrailPluginFactory factory = new ContrailPluginFactory(pluginName, sessionId, username, password, pluginNotificationHandler);
        factoryRepository().register(factory);
        return factory;
    }

    private PluginFactoryRepository factoryRepository() {
        return context.getBean(PluginFactoryRepository.class);
    }

    private void registerSingleton(String name, Object bean) {
        context.getDefaultListableBeanFactory().registerSingleton(name, bean);
    }

    @Override
    public void setServiceRegistry(IServiceRegistry serviceRegistry) {
        log.debug("Setting service registry.");

        registerSingleton("serviceRegistry", serviceRegistry);

        Object endpointService = serviceRegistry.getService("ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService");
        if (endpointService != null) {
            registerSingleton("endpointConfigurationService", endpointService);
        } else {
            log.error("Endpoint configuration service not found in the service registry.");
        }
    }

    @Override
    public void setPluginName(String pluginName) {
        log.debug("Setting plugin name: " + pluginName);
        this.pluginName = pluginName;
        registerSingleton("pluginName", pluginName);
        context.getDefaultListableBeanFactory()
               .addBeanPostProcessor(new PluginNameAwareBeanPostProcessor(pluginName));
    }

    @Override
    public void setPluginPublisher(IPluginPublisher pluginPublisher) {
        log.debug("Setting plugin publisher.");
        registerSingleton("pluginPublisher", pluginPublisher);
    }

    @Override
    public void addWatcher(PluginWatcher pluginWatcher) {
        log.debug("addWatcher() --> pluginWatcher: " + pluginWatcher.getId());
    }

    @Override
    public void removeWatcher(String pluginWatcherId) {
        log.debug("removeWatcher() --> pluginWatcherId: " + pluginWatcherId);
    }

    @Override
    public void registerEventPublisher(String type, String id, IPluginEventPublisher pluginEventPublisher) {
        log.debug("registerEventPublisher() --> type: " + type + ", id: " + id);
    }

    @Override
    public void unregisterEventPublisher(String type, String id, IPluginEventPublisher pluginEventPublisher) {
        log.debug("unregisterEventPublisher() --> type: " + type + ", id: " + id);
    }

    @Override
    public void installLicenses(PluginLicense[] licenses) throws PluginLicenseException {
        log.debug("installLicenses()");
    }

    @Override
    public void uninstallPluginFactory(IPluginFactory pluginFactory) {
        if(pluginFactory instanceof ContrailPluginFactory) {
            ContrailPluginFactory factory = (ContrailPluginFactory) pluginFactory;
            log.debug("Uninstalling factory for user: "+factory.getUsername());
            factoryRepository().unregister(factory);
        } else {
            log.error("Uninstall plugin factory called on unknown factory type.");
        }
    }
}
