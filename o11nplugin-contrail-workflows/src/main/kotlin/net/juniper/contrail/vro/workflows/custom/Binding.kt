/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.workflows.dsl.BasicParameterBuilder
import net.juniper.contrail.vro.workflows.dsl.fromListElementProperty

val rule = "rule"
val service = "service"

fun <T : Any> BasicParameterBuilder<T>.rulePropertyDataBinding() =
    fromListElementProperty(item, rule, "ruleProperty", parameterName, type)

fun <T : Any> BasicParameterBuilder<T>.servicePropertyDataBinding() =
    fromListElementProperty(item, service, "serviceProperty", parameterName, type)
