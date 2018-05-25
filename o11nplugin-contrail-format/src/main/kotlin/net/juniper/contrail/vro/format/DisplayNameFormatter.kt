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

    // `obj.parent?.name` returns null, so we use `obj.qualifiedName.dropLast(1).last()` to get the parent name.
    // TODO: Add endpoints to the string representation
    // TODO: make service use some pretty format instead of default `toString()`
    fun format(obj: FirewallRule): String? =
        "${obj.qualifiedName.dropLast(1).last()}: ${obj.actionList?.simpleAction} ${obj.direction} ${obj.service}"
}