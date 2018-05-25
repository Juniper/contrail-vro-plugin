/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.format

import ch.dunes.vso.sdk.api.IPluginFactory
import com.vmware.o11n.sdk.modeldriven.AbstractWrapper
import com.vmware.o11n.sdk.modeldriven.Findable
import com.vmware.o11n.sdk.modeldriven.ModelWrapper
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.VnSubnetsType

class ReferenceFormatter(val factory: IPluginFactory) {
    fun <T : ApiPropertyBase> format(wrapper: AbstractWrapper, references: List<ObjectReference<T>>?, type: String): String? {
        if (references == null) return null

        val parentSid: Sid = if (wrapper is Findable) wrapper.internalId else return null

        return references.asSequence().map { format(parentSid, type, it) }.filterNotNull().joinToString("\n")
    }

    private fun <T : ApiPropertyBase> format(parentSid: Sid, type: String, ref: ObjectReference<T>): String? {
        val sid = parentSid.with(type, ref.uuid)
        val element = factory.find(type, sid.toString()) as? ModelWrapper ?: return null
        val obj = element.__getTarget() as ApiObjectBase
        return format(obj, ref.attr)
    }

    private fun <T : ApiPropertyBase> format(obj: ApiObjectBase, attr: T?): String? = when (attr) {
        is VnSubnetsType -> format(obj as VirtualNetwork, attr)
        else -> format(obj)
    }

    private fun format(obj: ApiObjectBase) = when (obj) {
        is FloatingIp -> obj.address
        else -> obj.name
    }

    private fun format(virtualNetwork: VirtualNetwork, subnet: VnSubnetsType): String? {
        if (subnet.ipamSubnets == null || subnet.ipamSubnets.isEmpty()) return null
        return subnet.ipamSubnets.joinToString("\n") { format(virtualNetwork, it) }
    }

    private fun format(virtualNetwork: VirtualNetwork, subnet: IpamSubnetType): String =
        virtualNetwork.name + (subnet.subnet?.let { " (${PropertyFormatter.format(it)})" } ?: "")
}
