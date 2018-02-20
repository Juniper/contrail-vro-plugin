/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.FloatingIpPool
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.model.reference

internal fun deleteFloatingIpPoolWorkflow(): WorkflowDefinition {

    val workflowName = "Delete floating IP pool"

    return customWorkflow<FloatingIpPool>(workflowName).withScriptFile("deleteFloatingIpPool") {
        parameter(item, reference<FloatingIpPool>()) {
            description = "Floating IP pool to be deleted"
            mandatory = true
        }
    }
}
