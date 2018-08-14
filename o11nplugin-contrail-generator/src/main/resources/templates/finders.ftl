${editWarning}
package ${packageName}

import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.config.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports

private fun <T : ApiObjectBase> ConnectionRepository.query(clazz: Class<T>, query: String, key: String): List<FoundObject<T>> =
    connections.asSequence().map { it.query(clazz, query, key) }.filterNotNull().flatten().toList()

private fun <T : ApiObjectBase> Connection.query(clazz: Class<T>, query: String, key: String): List<FoundObject<T>>? =
    list(clazz)?.asSequence()
        ?.filter { query.isBlank() || it.name.startsWith(query) }
        ?.onEach { if (defaultConfig.context.readUponQuery.contains(clazz.simpleName)) read(it) }
        ?.map { FoundObject(it, info.sid.with(key, it.uuid)) }
        ?.toList()

<#list classes as klass>
class ${klass.simpleName}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${klass.simpleName}> {

    override fun assignId(obj: ${klass.simpleName}, sid: Sid): Sid =
        sid.with("${klass.pluginName}", obj.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${klass.simpleName}? {
        val connection = connections.getConnection(sid)
        return connection?.findById(${klass.simpleName}::class.java, sid.getString("${klass.pluginName}"))
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${klass.simpleName}>>? =
        connections.query(${klass.simpleName}::class.java, query, "${klass.pluginName}")
}

</#list>

<#list propertyRelations as relation>
class ${relation.childName}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${relation.childName}> {

    override fun assignId(obj: ${relation.childName}, sid: Sid) =
        sid

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${relation.childName}? {
        val connection = connections.getConnection(sid)
        val parent = connection?.findById(${relation.parentName}::class.java, sid.getString("${relation.parentPluginName}"))
        return parent?.${relation.propertyName}
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${relation.childName}>>? =
        null
}

</#list>

<#list categories as category>
class ${category.name}Finder : ObjectFinder<${category.name}> {

    override fun assignId(obj: ${category.name}, sid: Sid) =
        sid

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${category.name}? =
        ${category.name}

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${category.name}>>? =
        null
}

</#list>