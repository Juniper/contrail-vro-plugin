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

/*
<#list nestedRelations as relation>
    RELATION:
    ${relation.getter}
    ${relation.parentName}
    ${relation.childName}
    ${relation.parentCollapsedName}
    ${relation.childCollapsedName}
    ${relation.name}
    ${relation.getterSplitCamel}
    ${relation.getterDecapitalized}
    ${relation.getter}
    ${relation.toMany?c}
    ${relation.rootClass.simpleName}
    ${relation.parentWrapperName}
    ${relation.childWrapperName}
    :: PARAMS:
    <#list relation.simpleProperties as prop>
        SIMPLE PARAM:
        ${prop.className}
        ${prop.collapsedName}
        ${prop.propertyName}
        ${prop.componentName}
        ${prop.classLabel}
        ${prop.wrapperName}
    </#list>
    <#list relation.listProperties as prop>
        LIST PARAM:
        ${prop.className}
        ${prop.collapsedName}
        ${prop.propertyName}
        ${prop.componentName}
        ${prop.classLabel}
        ${prop.wrapperName}
    </#list>
    :: END OF PARAMS.
    :: GETTER CHAIN:
    <#list relation.getterChain as getterStuff>
        ${getterStuff}
    </#list>
    :: END OF GETTER CHAIN.
</#list>
*/

<#list nestedRelations as relation>
class ${relation.parentWrapperName}Has${relation.childWrapperName}
@Autowired constructor(private val connections: ConnectionRepository) : ObjectRelater<${relation.childWrapperName}> {

    override fun findChildren(ctx: PluginContext, relation: String, parentType: String, parentId: Sid): List<${relation.childWrapperName}>? {
        val connection = connections.getConnection(parentId)
        //TODO handle IOException
        val parent = connection?.findById(${relation.rootClass.simpleName}::class.java, parentId.getString("${relation.rootClass.simpleName}"))
        return listOf()
    }
}

</#list>