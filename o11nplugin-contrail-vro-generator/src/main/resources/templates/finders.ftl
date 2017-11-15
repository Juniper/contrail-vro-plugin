/***********************************************************
 *               GENERATED FILE - DO NOT EDIT              *
 ***********************************************************/
package net.juniper.contrail.vro.generated

import net.juniper.contrail.vro.config.ConnectionRepository
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired

class ConnectionFinder
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectFinder<Connection> {
    override fun assignId(connection: Connection, relatedObject: Sid?): Sid =
        connection.internalId

    override fun find(ctx: PluginContext, type: String, id: Sid): Connection? =
        connectionRepository.getConnection(id)

    override fun query(ctx: PluginContext, type: String, query: String): List<FoundObject<Connection>> =
        connectionRepository.findConnections(query).map { FoundObject<Connection>(it) }
}

private fun <T : ApiObjectBase> ConnectionRepository.query(clazz: Class<T>, query: String, key: String): List<FoundObject<T>> =
    connections.asSequence().map { it.query(clazz, query, key) }.filterNotNull().flatten().toList()

private fun <T : ApiObjectBase> Connection.query(clazz: Class<T>, query: String, key: String): List<FoundObject<T>>? =
    list(clazz)?.asSequence()
        ?.filter { query.isBlank() || it.name.startsWith(query) }
        ?.map { FoundObject(it, internalId.with(key, it.uuid)) }
        ?.toList()

<#list classes as klass>
class ${klass.simpleName}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${klass.simpleName}> {

    override fun assignId(obj: ${klass.simpleName}, sid: Sid): Sid =
        sid.with("${klass.simpleName}", obj.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${klass.simpleName}? {
        val connection = connections.getConnection(sid)
        //TODO handle IOException
        return connection?.findById(${klass.simpleName}::class.java, sid.getString("${klass.simpleName}"))
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${klass.simpleName}>>? =
        connections.query(${klass.simpleName}::class.java, query, "${klass.simpleName}")
}

</#list>