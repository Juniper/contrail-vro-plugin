/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

@file:JvmName("Custom")

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.model.Action

fun loadCustomWorkflows(schema: Schema, config: Config): List<WorkflowDefinition> =
    WorkflowLoader.loadSimple(schema, config).toList()

fun loadComplexWorkflows(definitions: List<WorkflowDefinition>, schema: Schema, config: Config): List<WorkflowDefinition> =
    WorkflowLoader.loadComplex(definitions, schema, config).toList()

fun loadCustomActions(version: String, packageName: String): List<Action> =
    ActionLoader.load().map { it(version, packageName) }.toList()