${editWarning}
package ${packageName}

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.config.ConnectionRepository
import org.springframework.beans.factory.annotation.Autowired

<#list rootClassNames as rootClass>
class ConnectionHas${rootClass}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${rootClass}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${rootClass}>? {
        val connection = connections.getConnection(parentId)
        //TODO handle IOException
        return connection?.list(${rootClass}::class.java)
    }
}
</#list>

<#list relations as relation>
class ${relation.parentName}Has${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        //TODO handle IOException
        val parent = connection?.findById(${relation.parentName}::class.java, parentId.getString("${relation.parentName}"))
        return connection?.getObjects(${relation.childName}::class.java, parent?.${relation.childNameDecapitalized}s)
    }
}

</#list>

<#list referenceRelations as relation>
class ${relation.parentName}Has${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        //TODO handle IOException
        val parent = connection?.findById(${relation.parentName}::class.java, parentId.getString("${relation.parentName}"))
        return connection?.getObjects(${relation.childOriginalName}::class.java, parent?.${relation.getter})?.map { it.as${relation.childName}() }
    }
}

</#list>

fun <T> toList(x: T?): List<T>? {
    if(x == null) return null
    return listOf(x)
}

<#list nestedRelations as relation>
class ${relation.parentWrapperName}Has${relation.childWrapperName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childWrapperName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childWrapperName}>? {
        val connection = connections.getConnection(parentId)
        //TODO handle IOException
        val parent = connection?.findById(${relation.rootClass.simpleName}::class.java, parentId.getString("${relation.rootClass.simpleName}"))
        return <#if relation.toMany == false>toList(</#if>parent<#list relation.getterChainWithStatus as nextGetter>?.${nextGetter.getGetterDecap()}<#if nextGetter.getGetterStatus() == true && nextGetter?has_next>?.get(parentId.getString("${nextGetter.getGetterName()}").toInt())</#if></#list><#if relation.toMany == true>?.mapIndexedNotNull { index, value -> value?.${relation.childWrapperName}(index) }<#else>?.${relation.childWrapperName}(null)</#if><#if relation.toMany == false>)</#if>
    }
}

</#list>