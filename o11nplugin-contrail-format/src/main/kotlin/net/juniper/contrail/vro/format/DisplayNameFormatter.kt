/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.format

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.QuotaType
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.format.PropertyFormatter.format

/**
 * Class allows to define custom formatting for selected inventory objects
 */
object DisplayNameFormatter {

    fun format(obj: FloatingIp): String? =
        obj.address

    fun format(obj: Subnet): String? =
        obj.ipPrefix?.let { format(it) }

    fun format(obj: ApiObjectBase): String? =
        obj.name

    fun format(obj: IpamSubnetType): String? =
        obj.subnet?.let { format(it) }

    fun format(obj: QuotaType): String? =
        "Quotas"

    fun format(obj: Tag): String? =
        if (obj.parentType == "project") "${obj.parentName}: ${obj.name}" else "global: ${obj.name}"

    fun format(obj: FirewallRule): String? {
        // `obj.parent?.name` returns null, so we use `obj.qualifiedName.dropLast(1).last()` to get the parent name.
        val parentName = obj.parentName.let { if (it == "default-policy-management") "global" else it }
        val simpleAction = obj.actionList?.simpleAction
        val direction = obj.direction
        val serviceGroup = obj.serviceGroup.run {
            if (this != null && this.isNotEmpty()) this[0].referredName.last() else ""
        }
        val service = if (obj.service != null && obj.service.protocol != null) format(obj.service) else serviceGroup
        val endpoint1 = format(obj.endpoint1)
        val endpoint2 = format(obj.endpoint2)
        return "$parentName: $simpleAction $service  EP1: $endpoint1  $direction  EP2: $endpoint2"
    }

    private val ApiObjectBase.parentName get() =
        qualifiedName.dropLast(1).last()

}