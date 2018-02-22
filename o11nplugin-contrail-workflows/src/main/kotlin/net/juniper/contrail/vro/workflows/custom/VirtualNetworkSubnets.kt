/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.getSubnetsOfVirtualNetwork
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.schema.propertyDescription

private val subnet = "subnet"
private val allocationPools = "allocationPools"
private val dnsServerAddress = "dnsServerAddress"

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
            parameter(subnet, string) {
                extractPropertyDescription<IpamSubnetType>(schema, title="CIDR")
                mandatory = true
                validWhen = isCidr()
            }
            parameter(allocationPools, string.array) {
                extractPropertyDescription<IpamSubnetType>(schema)
                mandatory = false
                validWhen = allocationPoolInSubnet(subnet)
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
                validWhen = addressInSubnet(subnet)
                mandatory = false
            }
            parameter("defaultGateway", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
                validWhen = addressIsFreeInSubnet(subnet, allocationPools, dnsServerAddress)
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

internal fun deleteSubnetWorkflow(): WorkflowDefinition {

    val workflowName = "Remove subnet from virtual network"

    return customWorkflow<VirtualNetwork>(workflowName).withScriptFile("deleteSubnet") {
        parameter(item, reference<VirtualNetwork>()) {
            description = "Virtual network to remove subnet from"
            mandatory = true
        }
        parameter("subnet", reference<Subnet>()) {
            description = "Subnet to be removed"
            mandatory = true
            visibility = WhenNonNull(item)
            browserRoot = item.asBrowserRoot()
            listedBy = actionCallTo(getSubnetsOfVirtualNetwork).parameter(item)
        }
    }
}