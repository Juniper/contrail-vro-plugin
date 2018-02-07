/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription

internal fun createSubnetWorkflow(schema: Schema): WorkflowDefinition {

    val workflowName = "Add subnet to virtual network"

    // Due to custom validation in Contrail UI the mandatory field is not extracted from schema

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("createSubnet") {
        step("References") {
            parameter("parent", reference<VirtualNetwork>()) {
                description = "Virtual network this subnet belongs to."
                mandatory = true
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = "IPAM this subnet uses."
                mandatory = true
            }
        }
        step("Parameters") {
            parameter("subnet", string) {
                extractPropertyDescription<IpamSubnetType>(schema, title="CIDR")
                mandatory = true
                customValidation = CIDR()
            }
            parameter("allocationPools", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
                mandatory = false
                customValidation = AllocationPool("subnet")
                multiline = true
            }
            parameter("addrFromStart", boolean) {
                // addr_from_start is the only parameter in IpamSubnet that has underscore in name
                description = """
                                Address from start
                                ${schema.propertyDescription<IpamSubnetType>("addr_from_start", false)}
                              """.trimIndent()
                mandatory = true
                defaultValue = true
            }
            parameter("dnsServerAddress", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
                customValidation = InCIDR("subnet")
                mandatory = false
            }
            parameter("defaultGateway", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
                customValidation = FreeInCIDR("subnet", "allocationPools", "dnsServerAddress")
                mandatory = true
            }
            parameter("enableDhcp", boolean) {
                extractPropertyDescription<IpamSubnetType>(schema, title="Enable DHCP")
                mandatory = true
                defaultValue = true
            }
        }
    }
}