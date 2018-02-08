${editWarning}
package ${packageName}

import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports

<#macro getterChain relation>
  <@compress single_line=true>
    parent
    <#list relation.getterChain as nextGetter>
      ?.${nextGetter.nameDecapitalized}
      <#if nextGetter.toMany == true>
        ?.get(sid.getString("${nextGetter.name}").toInt())
      </#if>
    </#list>
    ?.${relation.childWrapperName}(index)
  </@compress>
</#macro>

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
        sid.with("${klass.pluginName}", obj.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${klass.simpleName}? {
        val connection = connections.getConnection(sid)
        return connection?.findById(${klass.simpleName}::class.java, sid.getString("${klass.pluginName}"))
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${klass.simpleName}>>? =
        connections.query(${klass.simpleName}::class.java, query, "${klass.pluginName}")
}

</#list>

fun getIndex(sid: Sid, key: String): Int? {
    val potentialIndexStr: String? = sid.getString(key)
    return potentialIndexStr?.toIntOrNull()
}

<#list nestedRelations as relation>
class ${relation.childWrapperName}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${relation.childWrapperName}> {

    override fun assignId(obj: ${relation.childWrapperName}, sid: Sid): Sid {
        val sidKeyName = "${relation.getter}"
        val listIdx = obj.listIdx?.toString() ?: ""
        return sid.with(sidKeyName, listIdx)
    }
    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${relation.childWrapperName}? {
        val connection = connections.getConnection(sid)
        val parent = connection?.findById(${relation.rootClassSimpleName}::class.java, sid.getString("${relation.rootClassSimpleName}"))
        val index = getIndex(sid, "${relation.getter}")
        return <@getterChain relation/>
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${relation.childWrapperName}>>? =
        null
}

</#list>