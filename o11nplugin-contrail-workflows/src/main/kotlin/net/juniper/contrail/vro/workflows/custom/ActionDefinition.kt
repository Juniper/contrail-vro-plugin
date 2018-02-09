/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.isCidrAction
import net.juniper.contrail.vro.config.extractListProperty
import net.juniper.contrail.vro.config.getNetworkPolicyRules
import net.juniper.contrail.vro.config.isAllocPoolAction
import net.juniper.contrail.vro.config.isInCidrAction
import net.juniper.contrail.vro.config.isFreeInCidrAction
import net.juniper.contrail.vro.workflows.model.Action
import net.juniper.contrail.vro.workflows.model.Script
import net.juniper.contrail.vro.workflows.model.any
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.ofType
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.generateID

internal fun cidrValidationAction(version: String, packageName: String): Action {
    val name = isCidrAction
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

/** extractListProperty
 *
 * Action retrieves nested property that is located inside a list
 *
 * @param parentItem : Any - any inventory object
 * @param childItem : String - a human-readable representation of the object with it's index at the beginning
 * @param listAccessor: String - path to the list property
 * @param propertyPath: String - path to the final property
 */
internal fun extractListPropertyAction(version: String, packageName: String): Action {
    val name = extractListProperty
    val resultType = any
    val parameters = listOf(
        "parentItem" ofType any,
        "childItem" ofType string,
        "listAccessor" ofType string,
        "propertyPath" ofType string)
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

internal fun allocValidationAction(version: String, packageName: String): Action {
    val name = isAllocPoolAction
    val resultType = string
    val parameters = listOf("cidr" ofType string, "pools" ofType string)
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
    val parameters = listOf("cidr" ofType string, "ip" ofType string)
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
    val parameters = listOf("cidr" ofType string, "ip" ofType string, "pools" ofType string, "dns" ofType string)
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
