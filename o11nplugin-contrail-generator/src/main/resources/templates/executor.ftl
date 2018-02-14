${editWarning}
package net.juniper.contrail.vro.generated

import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import java.io.IOException

class Executor(private val connection: Connection) {

    <#list relations as relation>
    @Throws(IOException::class)
    fun create${relation.childPluginName}In${relation.parentName}(obj: ${relation.childName}, parent: ${relation.parentName}) {
        obj.setParent(parent)
        connection.create(obj)
    }
    </#list>

    <#list rootClasses as rootClass>
    @Throws(IOException::class)
    fun create${rootClass.pluginName}(obj: ${rootClass.simpleName}) {
        connection.create(obj)
    }
    </#list>

    <#list internalClasses as internalClass>
    @Throws(IOException::class)
    fun create${internalClass.pluginName}(obj: ${internalClass.simpleName}) {
        connection.create(obj)
    }
    </#list>

    <#list findableClasses as klass>
    @Throws(IOException::class)
    fun update${klass.pluginName}(obj: ${klass.simpleName}) {
        connection.update(obj)
    }

    @Throws(IOException::class)
    fun read${klass.pluginName}(obj: ${klass.simpleName}) {
        connection.read(obj)
    }

    @Throws(IOException::class)
    fun delete${klass.pluginName}(obj: ${klass.simpleName}) {
        connection.delete(obj)
    }
    </#list>

    <#list forwardRelations as relation>
    fun get${relation.childNamePluralized}Of${relation.parentPluginName}(parent: ${relation.parentName}) =
        connection.getObjects(${relation.childName}::class.java, parent.${relation.getter})

    </#list>
    fun getSubnetsOfVirtualNetwork(parent: VirtualNetwork): List<Subnet> {
        val ipams = parent.networkIpam ?: return emptyList()
        return ipams.asSequence().map {
            it.attr.ipamSubnets.asSequence().map { connection.findById<Subnet>(it.subnetUuid) }.filterNotNull()
        }.flatten().toList()
    }
}