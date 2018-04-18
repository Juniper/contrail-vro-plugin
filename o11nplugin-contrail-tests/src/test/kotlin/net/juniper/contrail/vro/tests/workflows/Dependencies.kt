package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.gen.FloatingIpPool_Wrapper
import net.juniper.contrail.vro.gen.FloatingIp_Wrapper
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.gen.Project_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper
import net.juniper.contrail.vro.gen.ServiceInstance_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterface_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import java.util.UUID

fun randomStringUuid() = UUID.randomUUID().toString()

class Dependencies(private val connection: Connection_Wrapper) {

    fun someProject(): Project_Wrapper {
        val res = Project_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someProject" + res.uuid
        res.setParentConnection(connection)
        return res
    }

    @JvmOverloads
    fun someVirtualNetwork(parent: Project_Wrapper = someProject()): VirtualNetwork_Wrapper {
        val res = VirtualNetwork_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someVirtualNetwork" + res.uuid
        res.setParentProject(parent)
        return res
    }

    @JvmOverloads
    fun someFloatingIpPool(parent: VirtualNetwork_Wrapper = someVirtualNetwork()): FloatingIpPool_Wrapper {
        val res = FloatingIpPool_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someFloatingIpPool" + res.uuid
        res.setParentVirtualNetwork(parent)
        return res
    }

    @JvmOverloads
    fun someNetworkIpam(parent: Project_Wrapper = someProject()): NetworkIpam_Wrapper {
        val res = NetworkIpam_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someNetworkIpam" + res.uuid
        res.setParentProject(parent)
        return res
    }

    @JvmOverloads
    fun somePort(parent: Project_Wrapper = someProject()): VirtualMachineInterface_Wrapper {
        val res = VirtualMachineInterface_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "somePort" + res.uuid
        res.setParentProject(parent)
        return res
    }

    @JvmOverloads
    fun someFloatingIp(parent: FloatingIpPool_Wrapper = someFloatingIpPool()): FloatingIp_Wrapper {
        val res = FloatingIp_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someFloatingIp" + res.uuid
        res.setParentFloatingIpPool(parent)
        return res
    }

    @JvmOverloads
    fun someNetworkPolicy(parent: Project_Wrapper = someProject()): NetworkPolicy_Wrapper {
        val res = NetworkPolicy_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someNetworkPolicy" + res.uuid
        res.setParentProject(parent)
        return res
    }

    @JvmOverloads
    fun someServiceInstance(parent: Project_Wrapper = someProject()): ServiceInstance_Wrapper {
        val res = ServiceInstance_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someServiceInstance" + res.uuid
        res.setParentProject(parent)
        return res
    }

    @JvmOverloads
    fun someSecurityGroup(parent: Project_Wrapper = someProject()): SecurityGroup_Wrapper {
        val res = SecurityGroup_Wrapper()
        res.uuid = randomStringUuid()
        res.name = "someSecurityGroup" + res.uuid
        res.setParentProject(parent)
        return res
    }

    fun somePortProperties(): VirtualMachineInterfacePropertiesType_Wrapper {
        return VirtualMachineInterfacePropertiesType_Wrapper()
    }
}
