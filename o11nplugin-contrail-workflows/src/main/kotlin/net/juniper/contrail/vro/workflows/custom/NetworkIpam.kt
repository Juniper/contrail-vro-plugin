/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.networkIpamSubnets
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.relationDescription
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName

// Due to custom validation in Contrail UI the mandatory field is not extracted from schema
internal fun createNetworkIpamSubnetWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<NetworkIpam>(addRelationWorkflowName<NetworkIpam, Subnet>()).withScriptFile("addSubnetToNetworkIpam") {
        step("References") {
            parameter(parent, reference<NetworkIpam>()) {
                description = "IPAM this subnet belongs to."
                mandatory = true
                validWhen = ipamHasAllocationMode(flat)
            }
        }
        ipamSubnetParameters(schema)
    }

internal fun removeNetworkIpamSubnetWorkflow(schema: Schema): WorkflowDefinition =
    customWorkflow<NetworkIpam>(removeRelationWorkflowName<NetworkIpam, Subnet>()).withScriptFile("removeSubnetFromNetworkIpam") {
        parameter(item, reference<NetworkIpam>()) {
            description = relationDescription<Project, NetworkIpam>(schema)
            mandatory = true
            validWhen = ipamHasAllocationMode(flat)
        }
        parameter("ipamSubnet", string) {
            visibility = WhenNonNull(item)
            description = "Subnet to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(networkIpamSubnets).parameter(item)
        }
    }