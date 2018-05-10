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
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.parentDescriptionInRemoveRelation
import net.juniper.contrail.vro.workflows.util.childDescriptionInRemoveRelation

internal fun addServiceInstanceToHealthCheck(schema: Schema): WorkflowDefinition {

    val workflowName = schema.addRelationWorkflowName<ServiceHealthCheck, ServiceInstance>()

    return customWorkflow<ServiceHealthCheck>(workflowName).withScriptFile("addServiceInstanceToHealthCheck") {
        step("References") {
            parameter(item, reference<ServiceHealthCheck>()) {
                description = schema.parentDescriptionInCreateRelation<ServiceHealthCheck, ServiceInstance>()
                mandatory = true
            }
            parameter(child, reference<ServiceInstance>()) {
                description = schema.childDescriptionInCreateRelation<ServiceHealthCheck, ServiceInstance>()
                mandatory = true
            }
            parameter("interface", string) {
                description = "Interface name"
                mandatory = true
                predefinedAnswersFrom = actionCallTo(serviceInstanceInterfaceNames).parameter(child)
            }
        }
    }
}

internal fun removeServiceInstanceFromHealthCheck(schema: Schema): WorkflowDefinition {

    val workflowName = removeRelationWorkflowName<ServiceHealthCheck, ServiceInstance>()

    return customWorkflow<ServiceHealthCheck>(workflowName).withScriptFile("removeServiceInstanceFromHealthCheck") {
        step("References") {
            parameter(item, reference<ServiceHealthCheck>()) {
                description = parentDescriptionInRemoveRelation<ServiceHealthCheck, ServiceInstance>()
                mandatory = true
            }
            parameter(child, reference<ServiceInstance>()) {
                description = childDescriptionInRemoveRelation<ServiceHealthCheck, ServiceInstance>()
                mandatory = true
            }
            parameter("interface", string) {
                description = "Interface name"
                mandatory = true
                predefinedAnswersFrom = actionCallTo(serviceInstanceInterfaceNames).parameter(child)
            }
        }
    }
}