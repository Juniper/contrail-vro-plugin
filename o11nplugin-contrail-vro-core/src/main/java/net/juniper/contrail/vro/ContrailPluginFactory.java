/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.vso.sdk.api.HasChildrenResult;
import ch.dunes.vso.sdk.api.IPluginFactory;
import ch.dunes.vso.sdk.api.IPluginNotificationHandler;
import ch.dunes.vso.sdk.api.PluginExecutionException;
import ch.dunes.vso.sdk.api.QueryResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.*;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ContrailPluginFactory implements IPluginFactory {

    private static final Logger log = LoggerFactory.getLogger(ContrailPluginFactory.class);

    private String pluginName;
    private String sessionId;
    private String username;
    private String password;
    private IPluginNotificationHandler pluginNotificationHandler;

    
    public ContrailPluginFactory(String pluginName, String sessionId, String username, String password, IPluginNotificationHandler pluginNotificationHandler) {
        log.debug("new ContrailPluginFactory() --> username: " + username);
        this.pluginName = pluginName;
        this.sessionId = sessionId;
        this.username = username;
        this.password = password;
        this.pluginNotificationHandler = pluginNotificationHandler;

    }

    @Override
    public void executePluginCommand(String cmd) throws PluginExecutionException {
        log.debug("executePluginCommand() --> cmd: " + cmd);
    }

    @Override
    public Object find(String type, String id) {
        return null;
    }

    @Override
    public QueryResult findAll(String type, String query) {
        log.debug("findAll() --> type: " + type + ", query: " + query);

        return new QueryResult();
    }

    @Override
    public HasChildrenResult hasChildrenInRelation(String parentType, String parentId, String relationName) {
        log.debug("hasChildrenInRelation() --> parentType: " + parentType + ", parentId: " + parentId + ", relationName: " + relationName);

        return HasChildrenResult.Unknown;
    }

    @Override
    public List<?> findRelation(String parentType, String parentId, String relationName) {
        log.debug("findRelation() --> parentType: " + parentType + ", parentId: " + parentId + ", relationName: " + relationName);

        
        return null;
    }

    @Override
    public void invalidate(String type, String id) {
        log.debug("invalidate() --> type: " + type + ", id: " + id);
    }

    @Override
    public void invalidateAll() {
        log.debug("invalidateAll()");
    }

    public String getUsername() {
        return username;
    }

    public IPluginNotificationHandler getPluginNotificationHandler() {
        return pluginNotificationHandler;
    }
}
