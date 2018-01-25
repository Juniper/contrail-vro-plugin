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
import net.juniper.contrail.vro.generator.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.generator.workflows.dsl.withScript
import net.juniper.contrail.vro.generator.workflows.dsl.workflow
import net.juniper.contrail.vro.generator.workflows.model.number
import net.juniper.contrail.vro.generator.workflows.model.reference
import net.juniper.contrail.vro.generator.workflows.model.string
import net.juniper.contrail.vro.generator.workflows.model.boolean
import net.juniper.contrail.vro.generator.workflows.model.array
import net.juniper.contrail.vro.generator.workflows.model.pair
import net.juniper.contrail.vro.generator.workflows.xsd.Schema
import net.juniper.contrail.vro.generator.workflows.xsd.propertyDescription
import net.juniper.contrail.vro.generator.workflows.xsd.relationDescription

fun createIpamSubnetWorkflow(info: ProjectInfo, schema: Schema): WorkflowDefinition {

    val workflowName = "Add subnet to virtual network"

    // Due to custom validation in Contrail UI the mandatory field is not extracted from schema

    return workflow(workflowName).withScript(loadFile(info.generatorRoot, "createIpamSubnet")) {
        step("References") {
            parameter("parent", reference<VirtualNetwork>()) {
                description = schema.relationDescription(Project::class.java, VirtualNetwork::class.java)
                mandatory = true
            }
            parameter("ipam", reference<NetworkIpam>()) {
                description = schema.relationDescription(VirtualNetwork::class.java, NetworkIpam::class.java)
                mandatory = true
            }
        }
        step("Subnet") {
            parameter("subnet_name", string) {
                description = schema.propertyDescription(IpamSubnetType::class.java, "subnet-name")
                mandatory = true
            }
            parameter("ip_prefix", string) {
                description = schema.propertyDescription(SubnetType::class.java, "ip-prefix")
                mandatory = true
            }
            parameter("ip_prefix_len", number) {
                description = schema.propertyDescription(SubnetType::class.java, "ip-prefix-len")
                mandatory = true
            }
        }
        step("Parameters") {
            parameter("addr_from_start", boolean) {

                // addr_from_start is the only parameter in IpamSubnet that has underscore in name

                description = schema.propertyDescription(IpamSubnetType::class.java, "addr_from_start")
                mandatory = true
                defaultValue = true
            }
            parameter("allocation_pools", array(pair("start", string, "end", string))) {
                description = schema.propertyDescription(IpamSubnetType::class.java, "allocation-pools")
                mandatory = true
            }
            parameter("enable_dhcp", boolean) {
                description = schema.propertyDescription(IpamSubnetType::class.java, "enable-dhcp")
                mandatory = true
                defaultValue = true
            }
            parameter("dns_server_address", string) {
                description = schema.propertyDescription(IpamSubnetType::class.java, "dns-nameservers")
                mandatory = false
            }
            parameter("default_gateway", string) {
                description = schema.propertyDescription(IpamSubnetType::class.java, "default-gateway")
            }
        }
    }
}