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
<#list nestedClasses as klass>
import ${klass.canonicalName}
</#list>

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

class ParameterCoordinates(val fieldPosition: Int, val listPosition: Int) {
    override fun toString(): String =
        "$fieldPosition.$listPosition"

    companion object {
        fun fromString(parameterId: String): ParameterCoordinates {
            val coords = parameterId.split(".").map { Integer.parseInt(it) }
            return ParameterCoordinates(coords.first(), coords.last())
        }
    }
}

<#list nestedRelations as relation>
/*
<#list relation.getterChainWithStatus as nextGetter>
    ${nextGetter.getGetterName()}
    ${nextGetter.getGetterStatus()?c}
</#list>
*/
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
        val parent = connection?.findById(${relation.rootClass.simpleName}::class.java, sid.getString("${relation.rootClass.simpleName}"))
        val potentialIndexStr = sid.getString("${relation.getter}")
        val potentialIndex: Int? = if(potentialIndexStr == "") {
            null
        } else {
            potentialIndexStr.toInt()
        }
        return parent<#list relation.getterChainWithStatus as nextGetter>?.${nextGetter.getGetterDecap()}<#if nextGetter.getGetterStatus() == true>?.get(sid.getString("${nextGetter.getGetterName()}").toInt())</#if></#list>?.${relation.childWrapperName}(potentialIndex)
    }

    override fun query(pluginContext: PluginContext, type: String, query: String): List<FoundObject<${relation.childWrapperName}>>? =
        null
}

</#list>