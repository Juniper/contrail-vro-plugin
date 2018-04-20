${editWarning}
package ${packageName}

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired
import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.base.ConnectionRepository

<#list rootClasses as rootClass>
class ConnectionHas${rootClass.simpleName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${rootClass.simpleName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${rootClass.simpleName}>? =
        connections.getConnection(parentId)?.run {
            list<${rootClass.simpleName}>()?.onEach { read(it) }
        }
}

</#list>

<#list relations as relation>
class ${relation.parentName}Has${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findById<${relation.parentName}>(parentId.getString("${relation.parentPluginName}"))
        return connection?.getObjects(${relation.childName}::class.java, parent?.${relation.childNameDecapitalized}s)
    }
}

</#list>

<#list forwardRelations as relation>
class ${relation.parentName}To${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findById(${relation.parentName}::class.java, parentId.getString("${relation.parentPluginName}"))
        return connection?.getObjects(${relation.childName}::class.java, parent?.${relation.getter})
    }
}

</#list>

<#list propertyRelations as relation>
class ${relation.parentName}Has${relation.childName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findById(${relation.parentName}::class.java, parentId.getString("${relation.parentPluginName}"))
        val child = parent?.${relation.propertyName} ?: return null
        return listOf(child)
    }
}

</#list>