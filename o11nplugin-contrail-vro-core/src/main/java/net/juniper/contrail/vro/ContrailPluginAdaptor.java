/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import javax.security.auth.login.LoginException;

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

public class ContrailPluginAdaptor implements IPluginAdaptor {

    private static final Logger log = LoggerFactory.getLogger(ContrailPluginAdaptor.class);

    private String pluginName;
    
    private IPluginPublisher pluginPublisher;

    public IPluginFactory createPluginFactory(String sessionId, String username, String password, IPluginNotificationHandler pluginNotificationHandler) throws SecurityException, LoginException, PluginLicenseException {
        log.debug("createPluginFactory() --> sessionId: " + sessionId + ", username: " + username);
        //TODO attach repository
        return new ContrailPluginFactory(pluginName, sessionId, username, password, pluginNotificationHandler);
    }

    @Override
    public void setPluginName(String pluginName) {
        log.debug("setPluginName() --> pluginName: " + pluginName);
        this.pluginName = pluginName;
    }

    @Override
    public void setPluginPublisher(IPluginPublisher pluginPublisher) {
        log.debug("setPluginPublisher()");
        this.pluginPublisher = pluginPublisher;
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
        log.debug("uninstallPluginFactory()");
    }
}
