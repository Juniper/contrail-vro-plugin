/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.vro.base.ConnectionRepository
import org.springframework.beans.factory.annotation.Autowired

class RootHasConnections @Autowired
constructor(private val connectionRepository: ConnectionRepository) : ObjectRelater<Connection>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid?): List<Connection> =
        connectionRepository.connections
}