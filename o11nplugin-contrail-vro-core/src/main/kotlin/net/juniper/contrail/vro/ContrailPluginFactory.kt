/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.config.ConnectionManager
import org.slf4j.LoggerFactory
import ch.dunes.vso.sdk.api.HasChildrenResult
import ch.dunes.vso.sdk.api.IPluginFactory
import ch.dunes.vso.sdk.api.IPluginNotificationHandler
import ch.dunes.vso.sdk.api.PluginExecutionException
import ch.dunes.vso.sdk.api.QueryResult
import net.juniper.contrail.vro.config.ConnectionRepository
import net.juniper.contrail.vro.config.CONNECTION
import net.juniper.contrail.vro.config.ROOT
import net.juniper.contrail.vro.config.ROOT_HAS_CONNECTIONS
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE

@Component
@Scope(SCOPE_PROTOTYPE)
class ContrailPluginFactory(
    val connections: ConnectionManager,
    val connectionRepository: ConnectionRepository,
    val pluginNotificationHandler: IPluginNotificationHandler) : IPluginFactory {

    private val log = LoggerFactory.getLogger(ContrailPluginFactory::class.java)

    init {
        log.debug("Created new ContrailPluginFactory")
    }

    @Throws(PluginExecutionException::class)
    override fun executePluginCommand(cmd: String) {
        log.debug("executePluginCommand(cmd='{}')", cmd)
    }

    override fun find(type: String, id: String?): Any? {
        log.debug("find(type={}, id={})", type, id)

        if (type == CONNECTION && id != null)
            return connectionRepository.getConnection(id)

        return null
    }

    private fun <T> List<T>.toResult(): QueryResult {
        val result = QueryResult()
        forEach(result::addElement)
        result.totalCount = size.toLong()
        return result
    }

    override fun findAll(type: String, query: String): QueryResult {
        log.debug("findAll(type={}, query='{}')", type, query)

        if (type == CONNECTION) {
            val connections = connectionRepository.findConnections(query)
            log.debug("Found {} connections", connections.size)
            return connections.toResult()
        }

        return QueryResult()
    }

    override fun hasChildrenInRelation(parentType: String, parentId: String?, relationName: String): HasChildrenResult {
        log.debug("hasChildrenInRelation(parentType={}, parentId={}, relationName={})", parentType, parentId, relationName)

        return HasChildrenResult.Unknown
    }

    override fun findRelation(parentType: String, parentId: String?, relationName: String?): List<*>? {
        log.debug("findRelation() --> parentType: $parentType, parentId: $parentId, relationName: $relationName")

        if (parentType == ROOT && relationName == ROOT_HAS_CONNECTIONS)
            return connectionRepository.connections

        return null
    }

    override fun invalidate(type: String?, id: String) {
        log.debug("invalidate(type={}, id={})", type, id)
    }

    override fun invalidateAll() {
        log.debug("invalidateAll()")
    }
}
