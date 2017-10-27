/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.ConnectionRepository
import net.juniper.contrail.vro.config.PROJECT
import org.springframework.beans.factory.annotation.Autowired

class RootHasConnections @Autowired
constructor(private val connectionRepository: ConnectionRepository) : ObjectRelater<Connection>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid?): List<Connection> =
        connectionRepository.connections
}

class ConnectionHasProjects
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<Project>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<Project>? {
        val connection = connections.getConnection(parentId) ?: return null
        //TODO handle IOException
        return connection.connector.list(Project::class.java, null) as List<Project>?
    }
}

class ProjectHasVirtualNetworks
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<VirtualNetwork> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<VirtualNetwork>? {
        val connection = connections.getConnection(parentId) ?: return null
        //TODO handle IOException
        val project = connection.connector.findById(Project::class.java, parentId.getString(PROJECT)) as Project
        return project.virtualNetworks
                .map { connection.connector.findById(VirtualNetwork::class.java, it.uuid) as VirtualNetwork }
    }
}

class ProjectHasNetworkPolicys
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<NetworkPolicy> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<NetworkPolicy>? {
        val connection = connections.getConnection(parentId) ?: return null
        //TODO handle IOException
        val project = connection.connector.findById(Project::class.java, parentId.getString(PROJECT)) as Project
        return project.networkPolicys
                .map { connection.connector.findById(NetworkPolicy::class.java, it.uuid) as NetworkPolicy }
    }
}