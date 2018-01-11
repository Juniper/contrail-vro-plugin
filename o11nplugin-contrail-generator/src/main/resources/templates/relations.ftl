${editWarning}
package ${packageName}

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.config.ConnectionRepository
import org.springframework.beans.factory.annotation.Autowired

<#macro getterChain relation>
  <@compress single_line=true>
    <#if relation.toMany == false>toList(</#if>
    parent
    <#list relation.getterChain as nextGetter>
      ?.${nextGetter.nameDecapitalized}
      <#if nextGetter.toMany == true && nextGetter?has_next>
        ?.get(parentId.getString("${nextGetter.name}").toInt())
      </#if>
    </#list>
    <#if relation.toMany == true>
      ?.mapIndexedNotNull { index, value -> value?.${relation.childWrapperName}(index) }
    <#else>
      ?.${relation.childWrapperName}(null)
    </#if>
    <#if relation.toMany == false>)</#if>
  </@compress>
</#macro>

<#list rootClassNames as rootClass>
class ConnectionHas${rootClass}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${rootClass}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${rootClass}>? {
        val connection = connections.getConnection(parentId)
        return connection?.list(${rootClass}::class.java)
    }
}

</#list>

<#list relations as relation>
class ${relation.parentName}Has${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findById(${relation.parentName}::class.java, parentId.getString("${relation.parentName}"))
        return connection?.getObjects(${relation.childName}::class.java, parent?.${relation.childNameDecapitalized}s)
    }
}

</#list>

<#list forwardRelations as relation>
class ${relation.parentName}Has${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findById(${relation.parentName}::class.java, parentId.getString("${relation.parentName}"))
        return connection?.getObjects(${relation.childName}::class.java, parent?.${relation.getter})
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
        val parent = connection?.findById(${relation.rootClassSimpleName}::class.java, parentId.getString("${relation.rootClassSimpleName}"))
        return <@getterChain relation/>
    }
}

</#list>