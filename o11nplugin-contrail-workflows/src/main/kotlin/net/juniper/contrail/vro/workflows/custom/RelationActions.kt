/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.ServiceGroup
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.addressGroupSubnets
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.networkIpamSubnets
import net.juniper.contrail.vro.config.networkOfServiceInterface
import net.juniper.contrail.vro.config.networkPolicyRules
import net.juniper.contrail.vro.config.portsForServiceInterface
import net.juniper.contrail.vro.config.routeTableRoutes
import net.juniper.contrail.vro.config.serviceGroupServices
import net.juniper.contrail.vro.config.serviceInstanceInterfaceNames
import net.juniper.contrail.vro.config.virtualNetworkSubnets
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

val networkPolicyRulesAction = ActionDefinition(
    name = networkPolicyRules,
    resultType = array(string),
    parameters = listOf(item ofType any)
)

val virtualNetworkSubnetsAction = ActionDefinition (
    name = virtualNetworkSubnets,
    resultType = array(string),
    parameters = listOf(item ofType reference<VirtualNetwork>())
)

val networkIpamSubnetsAction = ActionDefinition(
    name = networkIpamSubnets,
    resultType = array(string),
    parameters = listOf(item ofType reference<NetworkIpam>())
)

val addressGroupSubnetsAction = ActionDefinition(
    name = addressGroupSubnets,
    resultType = array(string),
    parameters = listOf(item ofType reference<AddressGroup>())
)

val serviceGroupServicesAction = ActionDefinition(
    name = serviceGroupServices,
    resultType = array(string),
    parameters = listOf(item ofType reference<ServiceGroup>())
)

val routeTableRoutesAction = ActionDefinition(
    name = routeTableRoutes,
    resultType = array(string),
    parameters = listOf(item ofType any)
)

val networkOfServiceInterfaceAction = ActionDefinition(
    name = networkOfServiceInterface,
    resultType = reference<VirtualNetwork>(),
    parameters = listOf(
        "serviceInstance" ofType reference<ServiceInstance>(),
        "name" ofType string
    )
)

val portsForServiceInterfaceAction = ActionDefinition(
    name = portsForServiceInterface,
    resultType = array(reference<VirtualMachineInterface>()),
    parameters = listOf(
        "serviceInstance" ofType reference<ServiceInstance>(),
        "name" ofType string
    )
)

val serviceInstanceInterfaceNamesAction = ActionDefinition (
    name = serviceInstanceInterfaceNames,
    resultType = array(string),
    parameters = listOf("serviceInstance" ofType reference<ServiceInstance>())
)
