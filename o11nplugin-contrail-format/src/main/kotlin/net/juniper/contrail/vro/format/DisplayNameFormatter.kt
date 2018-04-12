/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.format

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.Subnet

/**
 * Class allows to define custom formatting for selected inventory objects
 */
object DisplayNameFormatter {

    fun format(obj: FloatingIp):String? =
        obj.address

    fun format(obj: Subnet):String? =
        obj.ipPrefix?.let { PropertyFormatter.format(it) }

    fun format(obj: ApiObjectBase):String? =
        obj.name

    fun format(obj: IpamSubnetType): String? =
        obj.subnet?.let { PropertyFormatter.format(it) }
}