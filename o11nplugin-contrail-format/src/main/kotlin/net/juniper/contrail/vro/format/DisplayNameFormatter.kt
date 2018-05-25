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

/**
 * Class allows to define custom formatting for selected inventory objects
 */
object DisplayNameFormatter {

    fun format(obj: FloatingIp): String? =
        obj.address

    fun format(obj: Subnet): String? =
        obj.ipPrefix?.let { PropertyFormatter.format(it) }

    fun format(obj: ApiObjectBase): String? =
        obj.name

    fun format(obj: IpamSubnetType): String? =
        obj.subnet?.let { PropertyFormatter.format(it) }

    fun format(obj: QuotaType): String? =
        "Quotas"

    fun format(obj: FirewallRule): String? {
        // `obj.parent?.name` returns null, so we use `obj.qualifiedName.dropLast(1).last()` to get the parent name.
        val parentName = obj.qualifiedName.dropLast(1).last()
        val simpleAction = obj.actionList?.simpleAction
        val direction = obj.direction
        val service = if (obj.service != null) PropertyFormatter.format(obj.service) else obj.serviceGroup[0].referredName.last()
        val endpoint1 = PropertyFormatter.format(obj.endpoint1)
        val endpoint2 = PropertyFormatter.format(obj.endpoint2)
        return "$parentName: $simpleAction $service  EP1: $endpoint1  $direction  EP2: $endpoint2"
    }
}