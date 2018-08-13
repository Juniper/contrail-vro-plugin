/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.format

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.ApplicationPolicySet
import net.juniper.contrail.api.types.FirewallPolicy
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.QuotaType
import net.juniper.contrail.api.types.ServiceGroup
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
        val draftState = draftState(obj.draftModeState)
        // for draft rules, we need to determine the draft-policy-management's parent.
        val parentName = when (obj.parentName) {
            "default-policy-management" -> "global"
            "draft-policy-management" -> obj.grandparentName ?: "global"
            else -> obj.parentName
        }
        val simpleAction = obj.actionList?.simpleAction
        val direction = obj.direction
        val serviceGroup = obj.serviceGroup.run {
            if (this != null && this.isNotEmpty()) this[0].referredName.last() else ""
        }
        val service = if (obj.service != null && obj.service.protocol != null) format(obj.service) else serviceGroup
        val endpoint1 = format(obj.endpoint1)
        val endpoint2 = format(obj.endpoint2)
        val matchTags = obj.matchTags?.tagList?.joinToString(",") ?: "-"
        return "$draftState$parentName: $simpleAction $service  EP1: $endpoint1  $direction  EP2: $endpoint2  MT: $matchTags"
    }

    fun format(obj: FirewallPolicy): String? =
        "${draftState(obj.draftModeState)}${obj.name}"

    fun format(obj: ApplicationPolicySet): String? =
        "${draftState(obj.draftModeState)}${obj.name}"

    fun format(obj: ServiceGroup): String? =
        "${draftState(obj.draftModeState)}${obj.name}"

    fun format(obj: AddressGroup): String? =
        "${draftState(obj.draftModeState)}${obj.name}"

    // requires no space before next word
    private fun draftState(draftModeState: String?): String {
        draftModeState ?: return ""
        return "[DRAFT ($draftModeState)] "
    }

    // `obj.parent?.name` returns null, so we extract the parent name from the qualified name.
    private val ApiObjectBase.parentName get() =
        qualifiedName.dropLast(1).last()

    private val ApiObjectBase.grandparentName get() =
        qualifiedName.dropLast(2).lastOrNull()
}