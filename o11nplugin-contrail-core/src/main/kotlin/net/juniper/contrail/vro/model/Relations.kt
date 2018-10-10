/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.config.Configuration
import net.juniper.contrail.vro.config.GlobalDraftSecurity
import net.juniper.contrail.vro.config.GlobalSecurity
import org.springframework.beans.factory.annotation.Autowired

class RootHasConnections @Autowired
constructor(private val connectionRepository: ConnectionRepository) : ObjectRelater<Connection>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid?): List<Connection> =
        connectionRepository.connections
}

class ConnectionHasGlobalSecurity : ObjectRelater<GlobalSecurity>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, id: Sid) =
        listOf(GlobalSecurity)
}

class ConnectionHasGlobalDraftSecurity : ObjectRelater<GlobalDraftSecurity>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, id: Sid) =
        listOf(GlobalDraftSecurity)
}

class ConnectionHasConfiguration : ObjectRelater<Configuration>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, id: Sid): List<Configuration> =
        listOf(Configuration)
}

class NetworkIpamToSubnet @Autowired
constructor(private val connectionRepository: ConnectionRepository) : ObjectRelater<IpamSubnetType>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, id: Sid): List<IpamSubnetType>? {
        val vnId = id.getString("VirtualNetwork") ?: return null
        val connection = connectionRepository.getConnection(id) ?: return null
        val ipam = connection.find<NetworkIpam>(id) ?: return null
        if (ipam.ipamSubnetMethod == "flat-subnet") return null
        return ipam.virtualNetworkBackRefs.asSequence().filter { it.uuid == vnId }
                .flatMap { it.attr.ipamSubnets.asSequence() }.toList()
    }
}