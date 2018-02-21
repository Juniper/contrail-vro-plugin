/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.Subnet
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.getSubnetsOfVirtualNetwork
import net.juniper.contrail.vro.config.isAllocPoolAction
import net.juniper.contrail.vro.config.isCidrAction
import net.juniper.contrail.vro.config.isIpAction
import net.juniper.contrail.vro.config.isInCidrAction
import net.juniper.contrail.vro.config.isFreeInCidrAction
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.generateID

internal fun getSubnetsOfVirtualNetworkAction(version: String, packageName: String): Action {
    val name = getSubnetsOfVirtualNetwork
    val resultType = array(reference<Subnet>())
    val parameters = listOf("parent" ofType reference<VirtualNetwork>())
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

internal fun ipValidationAction(version: String, packageName: String): Action {
    val name = isIpAction
    val resultType = string
    val parameters = listOf("input" ofType string)
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

internal fun cidrValidationAction(version: String, packageName: String): Action {
    val name = isCidrAction
    val resultType = string
    val parameters = listOf("cidr" ofType string)
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

internal fun allocValidationAction(version: String, packageName: String): Action {
    val name = isAllocPoolAction
    val resultType = string
    val parameters = listOf("pools" ofType string, "cidr" ofType string)
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

internal fun inCidrValidationAction(version: String, packageName: String): Action {
    val name = isInCidrAction
    val resultType = string
    val parameters = listOf("ip" ofType string, "cidr" ofType string)
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

internal fun isFreeValidationAction(version: String, packageName: String): Action {
    val name = isFreeInCidrAction
    val resultType = string
    val parameters = listOf("ip" ofType string, "cidr" ofType string, "pools" ofType string, "dns" ofType string)
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
