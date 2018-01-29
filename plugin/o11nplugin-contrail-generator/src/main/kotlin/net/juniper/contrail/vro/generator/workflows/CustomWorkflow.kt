/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.SubnetType
import net.juniper.contrail.vro.generator.ProjectInfo
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.pair
import net.juniper.contrail.vro.workflows.util.extractPropertyDescription
import net.juniper.contrail.vro.workflows.util.extractRelationDescription
import net.juniper.contrail.vro.workflows.util.loadFile
import net.juniper.contrail.vro.workflows.schema.Schema

fun createIpamSubnetWorkflow(info: ProjectInfo, schema: Schema): WorkflowDefinition {

    val workflowName = "Add subnet to virtual network"

    // Due to custom validation in Contrail UI the mandatory field is not extracted from schema

    return workflow(workflowName).withScript(loadFile(info.generatorRoot, "createIpamSubnet")) {
        step("References") {
            parameter("parent", reference<VirtualNetwork>()) {
                extractRelationDescription<Project, VirtualNetwork>(schema)
                mandatory = true
            }
            parameter("ipam", reference<NetworkIpam>()) {
                extractRelationDescription<VirtualNetwork, NetworkIpam>(schema)
                mandatory = true
            }
        }
        step("Subnet") {
            parameter("subnet_name", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
                mandatory = true
            }
            parameter("ip_prefix", string) {
                extractPropertyDescription<SubnetType>(schema)
                mandatory = true
            }
            parameter("ip_prefix_len", number) {
                extractPropertyDescription<SubnetType>(schema)
                mandatory = true
            }
        }
        step("Parameters") {
            parameter("addr_from_start", boolean) {

                // addr_from_start is the only parameter in IpamSubnet that has underscore in name
                extractPropertyDescription<IpamSubnetType>(schema, convertParameterNameToXsd = false)
                mandatory = true
                defaultValue = true
            }
            parameter("allocation_pools", pair("start", string, "end", string).array) {
                extractPropertyDescription<IpamSubnetType>(schema)
                mandatory = true
            }
            parameter("enable_dhcp", boolean) {
                extractPropertyDescription<IpamSubnetType>(schema)
                mandatory = true
                defaultValue = true
            }
            parameter("dns_server_address", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
                mandatory = false
            }
            parameter("default_gateway", string) {
                extractPropertyDescription<IpamSubnetType>(schema)
            }
        }
    }
}