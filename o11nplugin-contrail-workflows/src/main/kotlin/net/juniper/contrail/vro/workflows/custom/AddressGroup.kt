/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.vro.config.addressGroupSubnets
import net.juniper.contrail.vro.config.constants.addLabelToAddressGroupWorkflowName
import net.juniper.contrail.vro.config.constants.addSubnetToAddressGroupWorkflowName
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.removeLabelFromAddressGroupWorkflowName
import net.juniper.contrail.vro.config.constants.removeSubnetFromAddressGroupWorkflowName
import net.juniper.contrail.vro.config.constants.subnet
import net.juniper.contrail.vro.config.listTagsOfType
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.propertyDescription

// There is no good description of Address Group object in the schema.
internal fun addSubnetToAddressGroupWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = addSubnetToAddressGroupWorkflowName

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("addSubnetToAddressGroup") {

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

internal fun removeSubnetFromAddressGroupWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = removeSubnetFromAddressGroupWorkflowName

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
    val workflowName = addLabelToAddressGroupWorkflowName

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("addLabelToAddressGroup") {
        parameter(item, reference<AddressGroup>()) {
            description = "Address Group to add label to"
            mandatory = true
        }
        parameter("label", reference<Tag>()) {
            description = "Label to add"
            mandatory = true
            visibility = WhenNonNull(item)
            validWhen = matchesSecurityScope(item, false)
            listedBy = actionCallTo(listTagsOfType).parameter(item).string("label")
        }
    }
}

internal fun removeLabelFromAddressGroup(): WorkflowDefinition {
    val workflowName = removeLabelFromAddressGroupWorkflowName

    return customWorkflow<AddressGroup>(workflowName).withScriptFile("removeLabelFromAddressGroup") {
        parameter(item, reference<AddressGroup>()) {
            description = "Address Group to remove label from"
            mandatory = true
        }
        parameter("label", reference<Tag>()) {
            description = "Label to remove"
            mandatory = true
            visibility = WhenNonNull(item)
            validWhen = matchesSecurityScope(item, false)
            listedBy = actionCallTo(propertyValue).parameter(item).string("tag")
        }
    }
}