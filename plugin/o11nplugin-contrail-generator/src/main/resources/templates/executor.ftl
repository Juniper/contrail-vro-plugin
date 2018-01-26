${editWarning}
package net.juniper.contrail.vro.generated

import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import java.io.IOException

class Executor(private val connection: Connection) {

    <#list relations as relation>
    @Throws(IOException::class)
    fun create${relation.childName}(obj: ${relation.childName}, parent: ${relation.parentName}) {
        obj.setParent(parent)
        connection.create(obj)
    }
    </#list>

    <#list rootClasses as rootClass>
    @Throws(IOException::class)
    fun create${rootClass.simpleName}(obj: ${rootClass.simpleName}) {
        connection.create(obj)
    }
    </#list>

    <#list internalClasses as internalClass>
    @Throws(IOException::class)
    fun create${internalClass.simpleName}(obj: ${internalClass.simpleName}) {
        connection.create(obj)
    }
    </#list>

    <#list findableClasses as klass>
    @Throws(IOException::class)
    fun update${klass.simpleName}(obj: ${klass.simpleName}) {
        connection.update(obj)
    }

    @Throws(IOException::class)
    fun read${klass.simpleName}(obj: ${klass.simpleName}) {
        connection.read(obj)
    }

    @Throws(IOException::class)
    fun delete${klass.simpleName}(obj: ${klass.simpleName}) {
        connection.delete(obj)
    }
    </#list>

    <#list forwardRelations as relation>
    fun get${relation.childNamePluralized}Of${relation.parentName}(parent: ${relation.parentName}) =
        connection.getObjects(${relation.childName}::class.java, parent.${relation.getter})

    </#list>

    fun getVnSubnet(vnetwork: VirtualNetwork, ipam: NetworkIpam): VnSubnetsType =
        vnetwork.networkIpam.find { it.uuid == ipam.uuid }?.attr ?: VnSubnetsType()

    fun isNetworRelatedToIpam(vnetwork: VirtualNetwork, ipam: NetworkIpam): Boolean =
        vnetwork.networkIpam.any { it.uuid == ipam.uuid }
}