/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.model.Action

fun loadCustomWorkflows(schema: Schema): List<WorkflowDefinition> =
    WorkflowLoader.load(schema).toList()

fun loadCustomActions(version: String, packageName: String): List<Action> =
    ActionLoader.load().map { it(version, packageName) }.toList()