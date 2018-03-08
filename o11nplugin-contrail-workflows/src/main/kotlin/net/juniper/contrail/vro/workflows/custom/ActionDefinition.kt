/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.api.types.ServiceTemplate
import net.juniper.contrail.vro.config.getNetworkPolicyRules
import net.juniper.contrail.vro.config.isSingleAddressNetworkPolicyRuleAction
import net.juniper.contrail.vro.config.isSingleAddressSecurityGroupRuleAction
import net.juniper.contrail.vro.config.serviceHasInterfaceWithName
import net.juniper.contrail.vro.config.getNetworkOfServiceInterface
import net.juniper.contrail.vro.config.portsForServiceInterface
import net.juniper.contrail.vro.config.areValidCommunityAttributes
import net.juniper.contrail.vro.config.templateHasInterfaceWithName
import net.juniper.contrail.vro.config.getRouteTableRoutes
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.generateID

/**
 * getNetworkPolicyRules
 *
 * Action retrieves string representation o network policy rules
 *
 * @param netpolicy : Any - reference to network policy or security group
 */
internal fun getNetworkPolicyRulesAction(version: String, packageName: String): Action {
    val name = getNetworkPolicyRules
    val resultType = array(string)
    val parameters = listOf("netpolicy" ofType any)
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun getRouteTableRoutes(version: String, packageName: String): Action {
    val name = getRouteTableRoutes
    val resultType = array(string)
    val parameters = listOf("parent" ofType any)
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun serviceHasInterfaceWithName(version: String, packageName: String): Action {
    val name = serviceHasInterfaceWithName
    val resultType = boolean
    val parameters = listOf(
        "serviceInstance" ofType reference<ServiceInstance>(),
        "name" ofType string
    )
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun templateHasInterfaceWithName(version: String, packageName: String): Action {
    val name = templateHasInterfaceWithName
    val resultType = boolean
    val parameters = listOf(
            "serviceTemplate" ofType reference<ServiceTemplate>(),
            "name" ofType string
    )
    return Action(
            name = name,
            packageName = packageName,
            id = generateID(packageName, name),
            version = version,
            resultType = resultType,
            parameters = parameters,
            script = Script(ScriptLoader.load(name))
    )
}

internal fun getNetworkOfServiceInterface(version: String, packageName: String): Action {
    val name = getNetworkOfServiceInterface
    val resultType = reference<VirtualNetwork>()
    val parameters = listOf(
        "serviceInstance" ofType reference<ServiceInstance>(),
        "name" ofType string
    )
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun getPortsForServiceInterface(version: String, packageName: String): Action {
    val name = portsForServiceInterface
    val resultType = array(reference<VirtualMachineInterface>())
    val parameters = listOf(
        "serviceInstance" ofType reference<ServiceInstance>(),
        "name" ofType string
    )
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun isMultiAddressNetworkPolicyRuleAction(version: String, packageName: String): Action {
    val name = isSingleAddressNetworkPolicyRuleAction
    val resultType = string
    val parameters = listOf("input" ofType string, "networkPolicy" ofType any)
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun isMultiAddressSecurityGroupRuleAction(version: String, packageName: String): Action {
    val name = isSingleAddressSecurityGroupRuleAction
    val resultType = string
    val parameters = listOf("input" ofType string, "securityGroup" ofType any)
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}

internal fun areValidCommunityAttributes(version: String, packageName: String): Action {
    val name = areValidCommunityAttributes
    val resultType = string
    val parameters = listOf("communityAttributes" ofType array(string))
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(ScriptLoader.load(name))
    )
}