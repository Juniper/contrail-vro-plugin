package net.juniper.contrail.vro.generated

/* ************************
 *     GENERATED FILE     *
 *       DO NOT EDIT      *
 **************************/

import net.juniper.contrail.vro.config.ConnectionRepository
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired

<#list classes as klass>
class ${klass.simpleName}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${klass.simpleName}> {

    override fun assignId(obj: ${klass.simpleName}, sid: Sid): Sid =
        sid.with("${klass.simpleName}", obj.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${klass.simpleName}? {
        val connection = connections.getConnection(sid) ?: return null
        //TODO handle IOException
        return connection.connector.findById(${klass.simpleName}::class.java, sid.getString("${klass.simpleName}")) as ${klass.simpleName}?
    }

    override fun query(pluginContext: PluginContext, s: String, s1: String): List<FoundObject<${klass.simpleName}>>? =
        null
}

</#list>