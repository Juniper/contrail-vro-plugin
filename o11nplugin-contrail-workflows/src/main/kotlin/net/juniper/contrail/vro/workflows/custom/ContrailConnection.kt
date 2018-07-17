/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.vro.config.constants.Configuration
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.createContrailControllerConnectionWorkflowName
import net.juniper.contrail.vro.config.constants.deleteContrailControllerConnectionWorkflowName
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.dsl.inCategory
import net.juniper.contrail.vro.workflows.dsl.withScript
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.SecureString
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

internal fun createConnectionWorkflow() =
    workflow(createContrailControllerConnectionWorkflowName).withScript(createConnectionScriptBody) {
        step("Controller") {
            parameter("name", string) {
                description = "Connection name"
                mandatory = true
                defaultValue = "Controller"
            }
            parameter("host", string) {
                description = "Controller host"
                mandatory = true
            }
            parameter("port", number) {
                description = "Controller port"
                mandatory = true
                defaultValue = 8082
                min = 0
                max = 65535
            }
        }
        step("Credentials") {
            parameter("username", string) {
                description = "User name"
            }
            parameter("password", SecureString) {
                description = "User password"
            }
            parameter("authServer", string) {
                description = "Authentication server"
            }
        }
        step("Tenant") {
            parameter("tenant", string) {
                description = "Tenant"
            }
        }
    }.inCategory(Configuration)

internal fun deleteConnectionWorkflow() =
    workflow(deleteContrailControllerConnectionWorkflowName).withScript(deleteConnectionScriptBody) {
        parameter(item, Connection.reference) {
            description = "$Connection to delete"
            mandatory = true
            showInInventory = true
            dataBinding = fromAction(defaultConnection, type) {}
        }
    }.inCategory(Configuration)

private val createConnectionScriptBody = """
ContrailConnectionManager.create(name, host, port, username, password, authServer, tenant);
""".trimIndent()

private val deleteConnectionScriptBody = """
ContrailConnectionManager.delete($item);
""".trimIndent()