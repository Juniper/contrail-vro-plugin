package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.gen.FloatingIpPool_Wrapper
import net.juniper.contrail.vro.gen.Project_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import java.util.UUID

fun randomStringUuid() = UUID.randomUUID().toString()

class Dependencies(private val connection: Connection_Wrapper) {
    fun someProject(): Project_Wrapper {
        val res = Project_Wrapper()
        res.uuid = randomStringUuid()
        res.setParentConnection(connection)
        return res
    }

    fun someVirtualNetwork(): VirtualNetwork_Wrapper {
        val res = VirtualNetwork_Wrapper()
        res.uuid = randomStringUuid()
        res.setParentProject(someProject())
        return res
    }

    fun floatingIpPool(): FloatingIpPool_Wrapper {
        val res = FloatingIpPool_Wrapper()
        res.uuid = randomStringUuid()
        res.setParentVirtualNetwork(someVirtualNetwork())
        return res
    }

}

