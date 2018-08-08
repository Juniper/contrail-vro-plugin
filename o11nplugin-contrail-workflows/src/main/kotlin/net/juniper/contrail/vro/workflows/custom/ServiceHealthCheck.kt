/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.ServiceHealthCheck
import net.juniper.contrail.api.types.ServiceInstance
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.serviceInstanceInterfaceNames
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation

internal fun addServiceInstanceToHealthCheck(schema: Schema): WorkflowDefinition {

    val workflowName = addRelationWorkflowName<ServiceHealthCheck, ServiceInstance>()

    return customWorkflow<ServiceHealthCheck>(workflowName).withScriptFile("addServiceInstanceToHealthCheck") {
        parameter(item, reference<ServiceHealthCheck>()) {
            description = schema.parentDescriptionInCreateRelation<ServiceHealthCheck, ServiceInstance>()
            mandatory = true
        }
        parameter(child, reference<ServiceInstance>()) {
            description = schema.childDescriptionInCreateRelation<ServiceHealthCheck, ServiceInstance>()
            mandatory = true
            validWhen = isNotReferencedBy(item)
        }
        parameter("interface", string) {
            description = "Interface name"
            mandatory = true
            predefinedAnswersFrom = actionCallTo(serviceInstanceInterfaceNames).parameter(child)
        }
    }
}
