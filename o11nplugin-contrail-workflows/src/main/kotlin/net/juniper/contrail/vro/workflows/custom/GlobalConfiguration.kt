package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.GlobalSystemConfig
import net.juniper.contrail.api.types.GlobalVrouterConfig
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.editGlobalConfiguration
import net.juniper.contrail.vro.config.defaultConnection
import net.juniper.contrail.vro.config.parentConnection
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.propertyDescription
import net.juniper.contrail.vro.schema.simpleTypeConstraints
import net.juniper.contrail.vro.workflows.dsl.FromComplexPropertyValue
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.fromAction
import net.juniper.contrail.vro.workflows.dsl.inCategory
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.ParameterType
import net.juniper.contrail.vro.workflows.model.array
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.model.string

val securityDraftModeParameterName = "enableSecurityPolicyDraft"
val encapsulationPrioritiesParameterName = "encapsulationPriorities"
val forwardingModeParameterName = "forwardingMode"
val encapsulationPredefinedAnswers = listOf("MPLSoGRE", "MPLSoUDP", "VXLAN")

internal fun editGlobalConfig(schema: Schema): WorkflowDefinition =
    workflow(editGlobalConfiguration).withScriptFile("editGlobalConfig") {
        step("Connection") {
            parameter(parentConnection, Connection.reference) {
                description = "Contrail controller whose config will be changed"
                mandatory = true
                dataBinding = fromAction(defaultConnection, type) {}
            }
        }
        step("System config") {
            visibility = WhenNonNull(parentConnection)
            parameter(securityDraftModeParameterName, boolean) {
                description = schema.propertyDescription<GlobalSystemConfig>(securityDraftModeParameterName)
                mandatory = true
                dataBinding = globalSystemConfigParameter(parentConnection, securityDraftModeParameterName, boolean)
            }
        }
        step("Virtual Routers config") {
            visibility = WhenNonNull(parentConnection)
            parameter(encapsulationPrioritiesParameterName, array(string)) {
                description = schema.propertyDescription<GlobalVrouterConfig>(encapsulationPrioritiesParameterName)
                // for some reason, trying to extract these from schema returns an error
                predefinedAnswers = encapsulationPredefinedAnswers
                dataBinding = globalVrouterConfigParameter(parentConnection, "$encapsulationPrioritiesParameterName.encapsulation", array(string))
                mandatory = true
            }
            parameter(forwardingModeParameterName, string) {
                description = schema.propertyDescription<GlobalVrouterConfig>(forwardingModeParameterName)
                additionalQualifiers += schema.simpleTypeConstraints<GlobalVrouterConfig>(forwardingModeParameterName)
                dataBinding = globalVrouterConfigParameter(parentConnection, forwardingModeParameterName, string)
                mandatory = true
            }
        }
    }.inCategory("Global Configuration")

private fun <T : Any> globalSystemConfigParameter(connectionParam: String, parameterName: String, type: ParameterType<T>) =
    FromComplexPropertyValue(connectionParam, "globalSystemConfig().$parameterName", type)

private fun <T : Any> globalVrouterConfigParameter(connectionParam: String, parameterName: String, type: ParameterType<T>) =
    FromComplexPropertyValue(connectionParam, "globalVrouterConfig().$parameterName", type)