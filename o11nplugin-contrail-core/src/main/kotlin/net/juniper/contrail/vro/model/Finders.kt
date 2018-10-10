/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.config.Configuration
import net.juniper.contrail.vro.config.GlobalDraftSecurity
import net.juniper.contrail.vro.config.DraftSecurity
import net.juniper.contrail.vro.config.GlobalSecurity
import org.springframework.beans.factory.annotation.Autowired

class ConnectionFinder
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectFinder<Connection>
{
    override fun assignId(connection: Connection, id: Sid?): Sid =
        connection.info.sid

    override fun find(ctx: PluginContext, type: String, id: Sid): Connection? =
        connectionRepository.getConnection(id)

    override fun query(ctx: PluginContext, type: String, query: String): List<FoundObject<Connection>> =
        connectionRepository.findConnections(query).map { FoundObject<Connection>(it, it.info.sid) }
}

class GlobalSecurityFinder : ObjectFinder<GlobalSecurity>
{
    override fun assignId(security: GlobalSecurity, id: Sid) = id
    override fun find(ctx: PluginContext, type: String, id: Sid) = GlobalSecurity
    override fun query(ctx: PluginContext, type: String, query: String) = null
}

class GlobalDraftSecurityFinder : ObjectFinder<GlobalDraftSecurity>
{
    override fun assignId(security: GlobalDraftSecurity, id: Sid) = id
    override fun find(ctx: PluginContext, type: String, id: Sid) = GlobalDraftSecurity
    override fun query(ctx: PluginContext, type: String, query: String) = null
}

class DraftSecurityFinder : ObjectFinder<DraftSecurity>
{
    override fun assignId(security: DraftSecurity, id: Sid) = id
    override fun find(ctx: PluginContext, type: String, id: Sid) = DraftSecurity
    override fun query(ctx: PluginContext, type: String, query: String) = null
}

class ConfigurationFinder : ObjectFinder<Configuration>
{
    override fun assignId(security: Configuration, id: Sid) = id
    override fun find(ctx: PluginContext, type: String, id: Sid) = Configuration
    override fun query(ctx: PluginContext, type: String, query: String) = null
}

class IpamSubnetTypeFinder
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectFinder<IpamSubnetType>
{
    override fun assignId(subnet: IpamSubnetType, id: Sid): Sid =
        id.with("IpamSubnet", subnet.subnetUuid)

    override fun find(ctx: PluginContext, type: String, id: Sid): IpamSubnetType? {
        val connection = connectionRepository.getConnection(id)
        val subnetUuid = id.getString("IpamSubnet") ?: return null
        val parent = connection?.find<VirtualNetwork>(id) ?: return null
        return parent.networkIpam.asSequence().flatMap { it.attr.ipamSubnets.asSequence() }.find { it.subnetUuid == subnetUuid }
    }

    override fun query(ctx: PluginContext, type: String, query: String): List<FoundObject<IpamSubnetType>>? =
        null
}