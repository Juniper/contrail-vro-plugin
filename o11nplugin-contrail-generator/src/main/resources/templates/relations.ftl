${editWarning}
package ${packageName}

import com.vmware.o11n.sdk.modeldriven.ObjectRelater
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import org.springframework.beans.factory.annotation.Autowired
import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.base.ConnectionRepository
import net.juniper.contrail.vro.config.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection

private val rootObject: (ApiObjectBase) -> Boolean =
    { it.parentType == null || it.parentType == "domain" || it.parentType == "config-root" }

<#list rootClasses as rootClass>
class ConnectionHas${rootClass.simpleName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${rootClass.simpleName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${rootClass.simpleName}>? =
        connections.getConnection(parentId)?.run {
            list<${rootClass.simpleName}>()?.onEach { read(it) }?.filter(rootObject)
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

<#list securityClasses as klass>

class ProjectHasDraft${klass.simpleName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${klass.simpleName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${klass.simpleName}>? {
        val connection = connections.getConnection(parentId)
        val parentProject = connection?.findById<Project>(parentId.getString("Project"))
        val draftFqn = parentProject?.qualifiedName?.plus("draft-policy-management")?.joinToString(":")
        val parent = draftFqn?.run {
            connection.findByFQN<PolicyManagement>(this)
        }
        return connection?.getObjects(${klass.simpleName}::class.java, parent?.${klass.simpleNameDecapitalized}s)
    }
}

class GlobalSecurityHas${klass.simpleName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${klass.simpleName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${klass.simpleName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findByFQN<PolicyManagement>("default-policy-management")
        return connection?.getObjects(${klass.simpleName}::class.java, parent?.${klass.simpleNameDecapitalized}s)
    }
}

class GlobalDraftSecurityHas${klass.simpleName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${klass.simpleName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${klass.simpleName}>? {
        val connection = connections.getConnection(parentId)
        val parent = connection?.findByFQN<PolicyManagement>("draft-policy-management")
        return connection?.getObjects(${klass.simpleName}::class.java, parent?.${klass.simpleNameDecapitalized}s)
    }
}

</#list>

class ProjectHasDraftSecurity: ObjectRelater<DraftSecurity> {

   override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<DraftSecurity> =
       listOf(DraftSecurity)
}

class ConfigurationHasGlobalSystemConfig
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectRelater<GlobalSystemConfig>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, id: Sid): List<GlobalSystemConfig>? {
        val connection = connectionRepository.getConnection(id) ?: return null
        val config = connection.findByFQN<GlobalSystemConfig>("default-global-system-config") ?: return null
        return listOf(config)
    }
}

class ConfigurationHasGlobalVrouterConfig
@Autowired constructor(private val connectionRepository: ConnectionRepository) : ObjectRelater<GlobalVrouterConfig>
{
    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, id: Sid): List<GlobalVrouterConfig>? {
        val connection = connectionRepository.getConnection(id) ?: return null
        val config = connection.findByFQN<GlobalVrouterConfig>("default-global-system-config:default-global-vrouter-config") ?: return null
        return listOf(config)
    }
}

<#list categories as category>
class ${category.parentName}Has${category.name}: ObjectRelater<${category.name}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${category.name}> =
        listOf(${category.name})
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