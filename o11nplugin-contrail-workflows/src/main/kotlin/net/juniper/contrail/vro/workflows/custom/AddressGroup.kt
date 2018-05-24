/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.vro.config.addressGroupSubnets
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.subnet
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.propertyDescription

internal fun addSubnetToAddressGroupWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add subnet to address group"

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("addSubnetToAddressGroup") {
        // There is no good description of Address Group object in the schema.
        parameter(item, reference<AddressGroup>()) {
            description = "Address Group to add subnet to"
            mandatory = true
        }
        parameter(subnet, string) {
            description = propertyDescription<IpamSubnetType>(schema, title = "CIDR")
            mandatory = true
            validWhen = isSubnet()
        }
    }
}

internal fun removeSubnetFromAddressGroup(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove subnet from address group"

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("removeSubnetFromAddressGroup") {
        parameter(item, reference<AddressGroup>()) {
            description = "Address Group to remove subnet from"
            mandatory = true
        }
        parameter(subnet, string) {
            visibility = WhenNonNull(item)
            description = "Subnet to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(addressGroupSubnets).parameter(item)
        }
    }
}
