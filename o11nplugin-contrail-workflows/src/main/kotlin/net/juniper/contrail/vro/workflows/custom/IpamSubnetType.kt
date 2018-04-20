/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.vro.workflows.dsl.PresentationParametersBuilder
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.schema.Schema

internal fun PresentationParametersBuilder.ipamSubnetParameters(schema: Schema) {
    val subnet = "subnet"
    val allocationPools = "allocationPools"
    val dnsServerAddress = "dnsServerAddress"

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
        parameter("allocUnit", number) {
            description = propertyDescription<IpamSubnetType>(schema)
            mandatory = false
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
        parameter(dnsServerAddress, string) {
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