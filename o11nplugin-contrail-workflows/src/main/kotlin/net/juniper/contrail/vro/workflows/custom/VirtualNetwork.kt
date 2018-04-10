/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.ipamsOfVirtualNetwork
import net.juniper.contrail.vro.config.subnetsOfVirtualNetwork
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.relationDescription

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
                validWhen = isNotFlat()
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = "IPAM this subnet uses."
                mandatory = true
                validWhen = isUserDefined()
            }
        }
        ipamSubnetParameters(schema)
    }
}

internal fun addFlatIpamWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add flat network IPAM to virtual network"

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("addFlatIpamToNetwork") {
        step("References") {
            parameter(parent, reference<VirtualNetwork>()) {
                description = "Virtual network to which network IPAM with flat allocation mode should be added to."
                mandatory = true
                validWhen = isNotUserDefined()
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = relationDescription<VirtualNetwork, NetworkIpam>(schema)
                mandatory = true
                validWhen = isFlat()
            }
        }
    }
}

internal fun removeFlatIpamWorkflow(): WorkflowDefinition {

    val workflowName = "Remove flat network IPAM from virtual network"

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("removeFlatIpamFromNetwork") {
        step("References") {
            parameter(parent, reference<VirtualNetwork>()) {
                description = "Virtual network which flat network IPAM should be removed from."
                mandatory = true
                validWhen = isNotUserDefined()
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = "Network IPAM to be removed"
                visibility = WhenNonNull(parent)
                browserRoot = parent.asBrowserRoot()
                mandatory = true
                listedBy = actionCallTo(ipamsOfVirtualNetwork).parameter(parent)
                validWhen = isFlat()
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
        }
        parameter("subnet", string) {
            description = "Subnet to be removed"
            mandatory = true
            visibility = WhenNonNull(item)
            predefinedAnswersFrom = actionCallTo(subnetsOfVirtualNetwork).parameter(item)
        }
    }
}