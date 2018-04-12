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
import org.springframework.beans.factory.annotation.Autowired

class ConnectionFinder
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectFinder<Connection>
{
    override fun assignId(connection: Connection, relatedObject: Sid?): Sid =
        connection.info.sid

    override fun find(ctx: PluginContext, type: String, id: Sid): Connection? =
        connectionRepository.getConnection(id)

    override fun query(ctx: PluginContext, type: String, query: String): List<FoundObject<Connection>> =
        connectionRepository.findConnections(query).map { FoundObject<Connection>(it, it.info.sid) }
}

class IpamSubnetFinder
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