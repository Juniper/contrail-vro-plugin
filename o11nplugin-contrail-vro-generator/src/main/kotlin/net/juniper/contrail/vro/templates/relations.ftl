package net.juniper.contrail.vro.generated

/* ************************
 *     GENERATED FILE     *
 *       DO NOT EDIT      *
 **************************/

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.config.ConnectionRepository
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

<#list relations as relation>
class ${relation.parentClassName}Has${relation.childClassName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childClassName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childClassName}>? {
        val connection = connections.getConnection(parentId) ?: return null
        //TODO handle IOException
        val parent = connection.connector.findById(${relation.parentClassName}::class.java, parentId.getString("${relation.parentClassName}")) as ${relation.parentClassName}
        return parent.${relation.childClassNameDecapitalized}s
                .map { connection.connector.findById(${relation.childClassName}::class.java, it.uuid) as ${relation.childClassName} }
    }
}
</#list>