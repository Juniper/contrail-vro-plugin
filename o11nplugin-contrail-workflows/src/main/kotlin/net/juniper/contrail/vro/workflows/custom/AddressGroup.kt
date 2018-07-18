/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.addressGroupSubnets
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.subnet
import net.juniper.contrail.vro.config.listLabelTags
import net.juniper.contrail.vro.config.propertyValue
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

internal fun addLabelToAddressGroup(): WorkflowDefinition {
    val workflowName = "Add label to Address Group"

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("addLabelToAddressGroup") {
        parameter(item, reference<AddressGroup>()) {
            description = "Address Group to add label to"
            mandatory = true
        }
        parameter("label", reference<Tag>()) {
            description = "Label to add"
            mandatory = true
            validWhen = matchesSecurityScope(item, false)
            listedBy = actionCallTo(listLabelTags).parameter(item)
        }
    }
}

internal fun removeLabelFromAddressGroup(): WorkflowDefinition {
    val workflowName = "Remove label from Address Group"

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("removeLabelFromAddressGroup") {
        parameter(item, reference<AddressGroup>()) {
            description = "Address Group to remove label from"
            mandatory = true
        }
        parameter("label", reference<Tag>()) {
            description = "Label to remove"
            mandatory = true
            validWhen = matchesSecurityScope(item, false)
            listedBy = actionCallTo(propertyValue).parameter(item).string("tag")
        }
    }
}