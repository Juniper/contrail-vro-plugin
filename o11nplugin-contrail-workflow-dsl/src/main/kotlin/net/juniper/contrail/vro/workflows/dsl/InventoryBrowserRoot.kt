/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl
import net.juniper.contrail.vro.config.parentConnection as parentConnectionAction

abstract class InventoryBrowserRoot {
    abstract val ognl: String?
}

object DefaultBrowserRoot : InventoryBrowserRoot() {
    override val ognl: String? get() =
        null
}

class ParameterValueAsRoot(val parameter: String) : InventoryBrowserRoot() {
    override val ognl: String? get() =
        "#$parameter"
}

class RootFromAction(val action: ActionCall) : InventoryBrowserRoot() {
    override val ognl: String? get() =
        action.ognl
}

fun String.asBrowserRoot(): InventoryBrowserRoot =
    ParameterValueAsRoot(this)

fun ActionCall.asBrowserRoot(): InventoryBrowserRoot =
    RootFromAction(this)

fun ActionCallBuilder.asBrowserRoot() =
    create().asBrowserRoot()

val String.parentConnection get() =
    actionCallTo(parentConnectionAction).parameter(this).asBrowserRoot()