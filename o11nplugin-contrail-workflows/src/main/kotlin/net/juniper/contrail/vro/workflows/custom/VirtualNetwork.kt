/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.subnetsOfVirtualNetwork
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.model.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.propertyDescription

private val subnet = "subnet"
private val allocationPools = "allocationPools"
private val dnsServerAddress = "dnsServerAddress"

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
                description = propertyDescription<IpamSubnetType>(schema, title = "CIDR")
                mandatory = true
                validWhen = isSubnet()
            }
            parameter(allocationPools, string.array) {
                description = propertyDescription<IpamSubnetType>(schema)
                mandatory = false
                validWhen = allocationPoolInSubnet(subnet)
            }
            parameter("addrFromStart", boolean) {
                // addr_from_start is the only parameter in IpamSubnet that has underscore in name
                description = propertyDescription<IpamSubnetType>(schema,
                    convertParameterNameToXsd = false,
                    title = "Address from start",
                    schemaName = "addr_from_start")
                mandatory = true
                defaultValue = true
            }
            parameter("dnsServerAddress", string) {
                description = propertyDescription<IpamSubnetType>(schema)
                validWhen = addressInSubnet(subnet)
                mandatory = false
            }
            parameter("defaultGateway", string) {
                description = propertyDescription<IpamSubnetType>(schema)
                validWhen = addressIsFreeInSubnet(subnet, allocationPools, dnsServerAddress)
                mandatory = true
            }
            parameter("enableDhcp", boolean) {
                description = propertyDescription<IpamSubnetType>(schema, title = "Enable DHCP")
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
            listedBy = actionCallTo(subnetsOfVirtualNetwork).parameter(item)
        }
    }
}