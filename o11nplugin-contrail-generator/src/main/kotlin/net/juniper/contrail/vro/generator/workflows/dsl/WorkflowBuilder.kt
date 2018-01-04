/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.dsl

import com.google.common.hash.Hashing
import net.juniper.contrail.vro.generator.workflows.model.Binding
import net.juniper.contrail.vro.generator.workflows.model.Position
import net.juniper.contrail.vro.generator.workflows.model.WorkflowItem
import net.juniper.contrail.vro.generator.workflows.model.WorkflowItemType
import net.juniper.contrail.vro.generator.workflows.model.WorkflowScript
import net.juniper.contrail.vro.generator.workflows.model.boolean

fun generateID(packageName: String, displayName: String) =
    Hashing.md5().newHasher()
        .putString("$packageName.$displayName", Charsets.UTF_8)
        .hash().toString()

infix fun String.packagedIn(packageName: String) =
    WorkflowNameInfo(workflowName = this, workflowPackage = packageName)

infix fun WorkflowNameInfo.withVersion(version: String) =
    WorkflowVersionInfo(this, version)

data class WorkflowNameInfo(
    val workflowPackage: String,
    val workflowName: String,
    val id: String = generateID(workflowPackage, workflowName))

data class WorkflowVersionInfo(val nameInfo: WorkflowNameInfo, val version: String)

val END = WorkflowItem(0, WorkflowItemType.end, Position(330.0f, 10.0f))

val success = ParameterInfo("success", boolean)

val defaultScriptPosition = Position(150.0f, 20.0f)

fun scriptWorkflowItem(script: WorkflowScript, inBinding: Binding, outBinding: Binding) =
    WorkflowItem(1, WorkflowItemType.task, defaultScriptPosition, "Scriptable task", script, inBinding, outBinding)
