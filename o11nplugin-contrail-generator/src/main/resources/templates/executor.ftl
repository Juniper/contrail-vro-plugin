${editWarning}
package net.juniper.contrail.vro.generated

import net.juniper.contrail.api.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import java.io.IOException

class Executor(private val connection: Connection) {

    <#list findableClasses as klass>
    @Throws(IOException::class)
    fun create${klass.pluginName}(obj: ${klass.simpleName}) {
        connection.create(obj)
    }

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

    <#list relations as relation>
    fun get${relation.childPluginNamePluralized}Of${relation.parentPluginName}(parent: ${relation.parentName}) =
        connection.getObjects(${relation.childName}::class.java, parent.${relation.childNameDecapitalized}s)

    </#list>

    <#list forwardRelations as relation>
    fun get${relation.childNamePluralized}Of${relation.parentPluginName}(parent: ${relation.parentName}) =
        connection.getObjects(${relation.childName}::class.java, parent.${relation.getter})

    </#list>

    fun getPortsOfVirtualNetwork(child: VirtualNetwork) : List<VirtualMachineInterface>{
        val ports = child.virtualMachineInterfaceBackRefs ?: emptyList()
        return ports.asSequence().map { connection.findById<VirtualMachineInterface>(it.uuid) }.filterNotNull().toList()
    }

    fun getPortsOfPortTuple(child: PortTuple) : List<VirtualMachineInterface>{
        val ports = child.virtualMachineInterfaceBackRefs ?: emptyList()
        return ports.asSequence().map { connection.findById<VirtualMachineInterface>(it.uuid) }.filterNotNull().toList()
    }

    fun getProjectsOfFloatingIpPool(child: FloatingIpPool) : List<Project>{
        val projects = child.projectBackRefs ?: emptyList()
        return projects.asSequence().map { connection.findById<Project>(it.uuid) }.filterNotNull().toList()
    }

    fun getSubnetsOfVirtualNetwork(parent: VirtualNetwork): List<Subnet> {
        val ipams = parent.networkIpam ?: return emptyList()
        return ipams.asSequence().map {
            it.attr.ipamSubnets.asSequence().map { connection.findById<Subnet>(it.subnetUuid) }.filterNotNull()
        }.flatten().toList()
    }

    // InstanceIp should be in 1-1 relation to VMI, so only first element is chosen if it exists
    fun getInstanceIpOfPort(port: VirtualMachineInterface): InstanceIp? =
        port.instanceIpBackRefs?.getOrNull(0)?.uuid?.let { connection.findById(it) }

    fun serviceHasInterfaceWithName(serviceInstance: ServiceInstance, name: String): Boolean? {
        val template = connection.findById<ServiceTemplate>(serviceInstance.serviceTemplate[0].uuid)!!
        val interfaceNames = template.properties.interfaceType.map { it.serviceInterfaceType }
        return (name in interfaceNames)
    }

    fun getNetworkOfServiceInterface(serviceInstance: ServiceInstance, name: String): VirtualNetwork? {
        val template = connection.findById<ServiceTemplate>(serviceInstance.serviceTemplate[0].uuid)!!
        val interfaceNames = template.properties.interfaceType.map { it.serviceInterfaceType }
        val index = interfaceNames.indexOf(name)
        if(serviceInstance.properties.interfaceList.size <= index || index < 0) return null
        return connection.findByFQN(serviceInstance.properties.interfaceList[index].virtualNetwork)
    }
}