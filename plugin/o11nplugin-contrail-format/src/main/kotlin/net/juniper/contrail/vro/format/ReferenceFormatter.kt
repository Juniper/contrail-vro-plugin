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
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.VnSubnetsType

class ReferenceFormatter(val factory: IPluginFactory) {
    fun <T : ApiPropertyBase> getRefString(wrapper: AbstractWrapper, references: List<ObjectReference<T>>?, type: String): String? {
        if (references == null) return null

        val parentSid: Sid = if (wrapper is Findable) wrapper.internalId else return null

        return references.asSequence().map { format(parentSid, type, it) }.filterNotNull().joinToString("\n")
    }

    private fun <T : ApiPropertyBase> format(parentSid: Sid, type: String, ref: ObjectReference<T>): String? {
        val sid = parentSid.with(type, ref.uuid)
        val element = factory.find(type, sid.toString()) as? ModelWrapper ?: return null
        val obj = element.__getTarget() as ApiObjectBase
        return format(obj, ref.attr, sid)
    }

    private fun <T : ApiPropertyBase> format(obj: ApiObjectBase, attr: T?, sid: Sid): String? = when(attr) {
        is VnSubnetsType -> format(attr, sid)
        else -> obj.name
    }

    private fun format(subnet: VnSubnetsType, sid: Sid): String? {
        if (subnet.ipamSubnets == null || subnet.ipamSubnets.isEmpty()) return null
        return subnet.ipamSubnets.asSequence().map { format(it, sid) }.filterNotNull().joinToString("\n")
    }

    private fun format(subnet: IpamSubnetType, sid: Sid): String? {
        val vnId = sid.with("VirtualNetwork", subnet.subnetUuid)
        val network = factory.find("VirtualNetwork", vnId.toString()) as? VirtualNetwork? ?: return null
        return "${network.name} (${PropertyFormatter.format(subnet.subnet)})"
    }
}
