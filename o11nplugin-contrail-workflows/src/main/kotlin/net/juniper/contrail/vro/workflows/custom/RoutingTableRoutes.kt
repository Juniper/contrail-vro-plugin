/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.RouteTable
import net.juniper.contrail.api.types.RouteType
import net.juniper.contrail.vro.config.constants.parent
import net.juniper.contrail.vro.config.getRouteTableRoutes
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
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
    val workflowName = "Add route to route table"

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
                extractPropertyDescription<RouteType>(schema)
                mandatory = true
                validWhen = isCidr()
            }
            // As of 14.03.2018 12:11:04, there is no description for this parameter available in the schema
            parameter("nextHopType", string) {
                description = "Next hop type"
                predefinedAnswers = currentlyAllowedNextHopTypes
                mandatory = true
            }
            parameter("nextHop", string) {
                extractPropertyDescription<RouteType>(schema)
                mandatory = true
                validWhen = isIPAddress()
            }
            parameter("knownCommunityAttributes", array(string)) {
                description = "Known communities"
                sameValues = false
                predefinedAnswers = knownCommunityAttributes
            }
            parameter("customCommunityAttributes", array(string)) {
                description = "Custom communities"
                sameValues = false
                validWhen = isCommunityAttribute()
            }
        }
    }
}

internal fun removeTableRouteWorkflow(schema: Schema): WorkflowDefinition {
    val workflowName = "Remove route table route"

    return customWorkflow<RouteTable>(workflowName).withScriptFile("removeRouteFromTable") {
        parameter(parent, reference<RouteTable>()) {
            extractRelationDescription<Project, RouteTable>(schema)
            mandatory = true
        }
        parameter("route", string) {
            visibility = WhenNonNull(parent)
            description = "Route to remove"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(getRouteTableRoutes).parameter(parent)
        }
    }
}