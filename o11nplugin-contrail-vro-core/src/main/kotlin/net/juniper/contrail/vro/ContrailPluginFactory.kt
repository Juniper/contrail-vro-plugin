/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.ConnectionManager
import net.juniper.contrail.vro.config.CONNECTION_MANAGER
import org.slf4j.LoggerFactory
import ch.dunes.vso.sdk.api.HasChildrenResult
import ch.dunes.vso.sdk.api.IPluginFactory
import ch.dunes.vso.sdk.api.IPluginNotificationHandler
import ch.dunes.vso.sdk.api.PluginExecutionException
import ch.dunes.vso.sdk.api.QueryResult
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import sun.security.pkcs11.wrapper.Constants

@Component
@Scope(SCOPE_PROTOTYPE)
class ContrailPluginFactory(
    private val connections: ConnectionManager,
    val pluginNotificationHandler: IPluginNotificationHandler) : IPluginFactory {

    private val log = LoggerFactory.getLogger(ContrailPluginFactory::class.java)

    init {
        log.debug("Created new ContrailPluginFactory.")
    }

    @Throws(PluginExecutionException::class)
    override fun executePluginCommand(cmd: String) {
        log.debug("executePluginCommand() --> cmd: " + cmd)
    }

    override fun find(type: String, id: String): Any? {
        if(type == CONNECTION_MANAGER)
            return connections;
        return null
    }

    override fun findAll(type: String, query: String): QueryResult {
        log.debug("findAll() --> type: $type, query: $query")

        return QueryResult()
    }

    override fun hasChildrenInRelation(parentType: String, parentId: String, relationName: String): HasChildrenResult {
        log.debug("hasChildrenInRelation() --> parentType: $parentType, parentId: $parentId, relationName: $relationName")

        return HasChildrenResult.Unknown
    }

    override fun findRelation(parentType: String, parentId: String, relationName: String): List<*>? {
        log.debug("findRelation() --> parentType: $parentType, parentId: $parentId, relationName: $relationName")

        return null
    }

    override fun invalidate(type: String, id: String) {
        log.debug("invalidate() --> type: $type, id: $id")
    }

    override fun invalidateAll() {
        log.debug("invalidateAll()")
    }
}
