/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.asForwardRef
import net.juniper.contrail.vro.config.subnetsOfVirtualNetwork
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.parentDescriptionInRemoveRelation
import net.juniper.contrail.vro.workflows.util.childDescriptionInRemoveRelation

val flatOnly = "flat-subnet-only"
val userDefinedOnly = "user-defined-subnet-only"
val flat = "flat-subnet"

internal fun addPolicyToVirtualNetwork(schema: Schema): WorkflowDefinition {
    val workflowName = schema.addRelationWorkflowName<VirtualNetwork, NetworkPolicy>()
    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("addPolicyToVirtualNetwork") {
        parameter(item, reference<VirtualNetwork>()) {
            description = schema.parentDescriptionInCreateRelation<VirtualNetwork, NetworkPolicy>()
            mandatory = true
        }
        parameter("networkPolicy", reference<NetworkPolicy>()) {
            description = schema.childDescriptionInCreateRelation<VirtualNetwork, NetworkPolicy>()
            mandatory = true
        }
    }
}

internal fun createSubnetWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add subnet to virtual network"

    // Due to custom validation in Contrail UI the mandatory field is not extracted from schema

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("createSubnet") {
        step("References") {
            parameter(parent, reference<VirtualNetwork>()) {
                description = "Virtual network this subnet belongs to."
                mandatory = true
                validWhen = networkHasNotAllocationMode(flatOnly)
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = "IPAM this subnet uses."
                mandatory = true
                validWhen = ipamHasNotAllocationMode(flat)
            }
        }
        ipamSubnetParameters(schema)
    }
}

internal fun addFlatIpamWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = schema.addRelationWorkflowName<VirtualNetwork, NetworkIpam>()

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("addFlatIpamToNetwork") {
        step("References") {
            parameter(parent, reference<VirtualNetwork>()) {
                description = schema.parentDescriptionInCreateRelation<VirtualNetwork, NetworkIpam>()
                mandatory = true
                validWhen = networkHasNotAllocationMode(userDefinedOnly)
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = schema.childDescriptionInCreateRelation<VirtualNetwork, NetworkIpam>()
                mandatory = true
                validWhen = ipamHasAllocationMode(flat)
            }
        }
    }
}

internal fun removeFlatIpamWorkflow(): WorkflowDefinition {

    val workflowName = removeRelationWorkflowName<VirtualNetwork, NetworkIpam>()

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("removeFlatIpamFromNetwork") {
        step("References") {
            parameter(parent, reference<VirtualNetwork>()) {
                description = parentDescriptionInRemoveRelation<VirtualNetwork, NetworkIpam>()
                mandatory = true
                validWhen = networkHasNotAllocationMode(userDefinedOnly)
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = childDescriptionInRemoveRelation<VirtualNetwork, NetworkIpam>()
                visibility = WhenNonNull(parent)
                browserRoot = parent.asBrowserRoot()
                mandatory = true
                listedBy = actionCallTo(propertyValue).parameter(parent).string(asForwardRef<NetworkIpam>())
                validWhen = ipamHasAllocationMode(flat)
            }
        }
    }
}

internal fun deleteSubnetWorkflow(): WorkflowDefinition {

    val workflowName = "Remove subnet from virtual network"

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("deleteSubnet") {
        parameter(item, reference<VirtualNetwork>()) {
            description = "Virtual network to remove subnet from"
            mandatory = true
            validWhen = networkHasNotAllocationMode(flatOnly)
        }
        parameter("subnet", string) {
            description = "Subnet to be removed"
            mandatory = true
            visibility = WhenNonNull(item)
            predefinedAnswersFrom = actionCallTo(subnetsOfVirtualNetwork).parameter(item)
        }
    }
}