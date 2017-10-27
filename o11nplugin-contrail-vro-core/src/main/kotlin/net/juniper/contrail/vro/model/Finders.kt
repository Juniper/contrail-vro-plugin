/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.ConnectionRepository
import net.juniper.contrail.vro.config.NETWORK_POLICY
import net.juniper.contrail.vro.config.PROJECT
import net.juniper.contrail.vro.config.VIRTUAL_NETWORK
import org.springframework.beans.factory.annotation.Autowired

class ConnectionFinder
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectFinder<Connection>
{
    override fun assignId(connection: Connection, relatedObject: Sid?): Sid =
        connection.internalId

    override fun find(ctx: PluginContext, type: String, id: Sid): Connection? =
        connectionRepository.getConnection(id)

    override fun query(ctx: PluginContext, type: String, query: String): List<FoundObject<Connection>> =
        connectionRepository.findConnections(query).map { FoundObject<Connection>(it) }
}

class ProjectFinder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<Project>
{
    override fun assignId(project: Project, sid: Sid): Sid =
        sid.with(PROJECT, project.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): Project? {
        val connection = connections.getConnection(sid) ?: return null
        //TODO handle IOException
        return connection.connector.findById(Project::class.java, sid.getString(PROJECT)) as Project
    }

    override fun query(pluginContext: PluginContext, s: String, s1: String): List<FoundObject<Project>>? =
        null
}

class VirtualNetworkFinder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<VirtualNetwork> {

    override fun assignId(network: VirtualNetwork, sid: Sid): Sid =
        sid.with(VIRTUAL_NETWORK, network.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): VirtualNetwork? {
        val connection = connections.getConnection(sid) ?: return null
        //TODO handle IOException
        return connection.connector.findById(Project::class.java, sid.getString(VIRTUAL_NETWORK)) as VirtualNetwork
    }

    override fun query(pluginContext: PluginContext, s: String, s1: String): List<FoundObject<VirtualNetwork>>? =
        null
}

class NetworkPolicyFinder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<NetworkPolicy> {

    override fun assignId(policy: NetworkPolicy, sid: Sid): Sid =
        sid.with(NETWORK_POLICY, policy.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): NetworkPolicy? {
        val connection = connections.getConnection(sid) ?: return null
        //TODO handle IOException
        return connection.connector.findById(Project::class.java, sid.getString(NETWORK_POLICY)) as NetworkPolicy
    }

    override fun query(pluginContext: PluginContext, s: String, s1: String): List<FoundObject<NetworkPolicy>>? =
        null
}