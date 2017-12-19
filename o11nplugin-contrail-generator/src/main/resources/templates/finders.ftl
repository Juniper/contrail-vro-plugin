${editWarning}
package ${packageName}

import net.juniper.contrail.vro.config.ConnectionRepository
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import com.vmware.o11n.sdk.modeldriven.FoundObject
import com.vmware.o11n.sdk.modeldriven.ObjectFinder
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired

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

<#list classNames as klass>
class ${klass}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${klass}> {

    override fun assignId(obj: ${klass}, sid: Sid): Sid =
        sid.with("${klass}", obj.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${klass}? {
        val connection = connections.getConnection(sid)
        //TODO handle IOException
        return connection?.findById(${klass}::class.java, sid.getString("${klass}"))
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${klass}>>? =
        connections.query(${klass}::class.java, query, "${klass}")
}

</#list>

<#list referenceWrappers as wrapper>
class ${wrapper.referenceName}Finder
@Autowired constructor(private val connections: ConnectionRepository) : ObjectFinder<${wrapper.referenceName}> {

    override fun assignId(obj: ${wrapper.referenceName}, sid: Sid): Sid =
        sid.with("${wrapper.referenceName}", obj.uuid)

    override fun find(pluginContext: PluginContext, s: String, sid: Sid): ${wrapper.referenceName}? {
        val connection = connections.getConnection(sid)
        //TODO handle IOException
        return connection?.findById(${wrapper.className}::class.java, sid.getString("${wrapper.referenceName}"))?.as${wrapper.referenceName}()
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${wrapper.referenceName}>>? =
        connections.query(${wrapper.className}::class.java, query, "${wrapper.referenceName}")
            .map { FoundObject(it.`object`.as${wrapper.referenceName}(), it.id) }
}

</#list>

fun getIndex(sid: Sid, key: String): Int? {
    val potentialIndexStr = sid.getString(key)
    return if(potentialIndexStr == "") {
        null
    } else {
        potentialIndexStr.toInt()
    }
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
        //TODO handle IOException
        val parent = connection?.findById(${relation.rootClassSimpleName}::class.java, sid.getString("${relation.rootClassSimpleName}"))
        val index = getIndex(sid, "${relation.getter}")
        return <@getterChain relation/>
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${relation.childWrapperName}>>? =
        null
}

</#list>