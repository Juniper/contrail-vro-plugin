/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.serviceInstanceInterfaceNames
import net.juniper.contrail.vro.config.networkPolicyRules
import net.juniper.contrail.vro.config.subnetsOfVirtualNetwork
import net.juniper.contrail.vro.config.routeTableRoutes
import net.juniper.contrail.vro.config.portsForServiceInterface
import net.juniper.contrail.vro.config.networkIpamSubnets
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

val networkPolicyRulesAction = ActionDefinition(
    name = networkPolicyRules,
    resultType = array(string),
    parameters = listOf("netpolicy" ofType any)
)

val networkIpamSubnets = ActionDefinition(
    name = networkIpamSubnets,
    resultType = array(string),
    parameters = listOf("parent" ofType reference<NetworkIpam>())
)

val routeTableRoutesAction = ActionDefinition(
    name = routeTableRoutes,
    resultType = array(string),
    parameters = listOf("parent" ofType any)
)

val networkOfServiceInterfaceAction = ActionDefinition(
    name = net.juniper.contrail.vro.config.networkOfServiceInterface,
    resultType = reference<VirtualNetwork>(),
    parameters = listOf(
        "serviceInstance" ofType reference<ServiceInstance>(),
        "name" ofType string
    )
)

val subnetsOfVirtualNetworkAction = ActionDefinition (
    name = subnetsOfVirtualNetwork,
    resultType = array(reference<Subnet>()),
    parameters = listOf("parent" ofType reference<VirtualNetwork>())
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
