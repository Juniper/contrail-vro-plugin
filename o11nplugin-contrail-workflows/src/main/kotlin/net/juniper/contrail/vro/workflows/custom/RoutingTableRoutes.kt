/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.RouteTable
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.extractRelationDescription

val knownCommunityAttributes = listOf(
    "no-export",
    "accept-own",
    "no-advertise",
    "no-export-subconfed",
    "no-reoriginate"
)

// Schema has two possible nextHopTypes, but one of them is described to be used only internally.
val currentlyAllowedNextHopTypes = listOf(
    "ip-address"
)

internal fun addRouteToRoutingTableWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Add route to routing table"

    return customWorkflow<RouteTable>(workflowName).withScriptFile("addRouteToTable") {
        step("Parent route table") {
            parameter(parent, reference<RouteTable>()) {
                extractRelationDescription<Project, RouteTable>(schema)
                mandatory = true
            }
        }
        step("Route attributes") {
            visibility = WhenNonNull(parent)
            parameter("prefix", string) {
                mandatory = true
                validWhen = isCidr()
            }
            parameter("nextHopType", string) {
                predefinedAnswers = currentlyAllowedNextHopTypes
                mandatory = true
            }
            parameter("nextHop", string) {
                mandatory = true
                validWhen = isIPAddress()
            }
            parameter("knownCommunityAttributes", array(string)) {
                mandatory = true
                sameValues = false
                predefinedAnswers = knownCommunityAttributes
            }
            parameter("customCommunityAttributes", array(string)) {
                mandatory = true
                sameValues = false
                validWhen = isCommunityAttribute()
            }
        }
    }
}
