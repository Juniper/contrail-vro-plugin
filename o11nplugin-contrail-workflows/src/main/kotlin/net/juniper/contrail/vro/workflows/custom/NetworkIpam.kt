/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.networkIpamSubnets
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.relationDescription

internal fun removeNetworkIpamSubnetWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove network ipam subnet"

    return customWorkflow<NetworkIpam>(workflowName).withScriptFile("removeSubnetFromNetworkIpam") {
        parameter(parent, reference<NetworkIpam>()) {
            description = relationDescription<Project, NetworkIpam>(schema)
            mandatory = true
        }
        parameter("rule", string) {
            visibility = WhenNonNull(parent)
            description = "Subnet to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(networkIpamSubnets).parameter(parent)
        }
    }
}