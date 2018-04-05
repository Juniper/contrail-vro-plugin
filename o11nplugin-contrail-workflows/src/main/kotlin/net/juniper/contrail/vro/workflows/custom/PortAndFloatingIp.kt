/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.types.FloatingIp
import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.backRefPropertyName
import net.juniper.contrail.vro.config.constants.child
import net.juniper.contrail.vro.config.constants.item
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.propertyValue
import net.juniper.contrail.vro.config.refPropertyName
import net.juniper.contrail.vro.workflows.dsl.WhenNonNull
import net.juniper.contrail.vro.workflows.dsl.WorkflowDefinition
import net.juniper.contrail.vro.workflows.dsl.actionCallTo
import net.juniper.contrail.vro.workflows.dsl.asBrowserRoot
import net.juniper.contrail.vro.workflows.dsl.inCategory
import net.juniper.contrail.vro.workflows.dsl.workflow
import net.juniper.contrail.vro.workflows.model.boolean
import net.juniper.contrail.vro.workflows.model.reference
import net.juniper.contrail.vro.workflows.schema.Schema
import net.juniper.contrail.vro.workflows.util.addRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.removeRelationWorkflowName
import net.juniper.contrail.vro.workflows.util.propertyDescription
import net.juniper.contrail.vro.workflows.util.childDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.childDescriptionInRemoveRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInCreateRelation
import net.juniper.contrail.vro.workflows.util.parentDescriptionInRemoveRelation

internal fun addFloatingIpToPort(schema: Schema) =
    addPortFloatingIpRelation<VirtualMachineInterface, FloatingIp>(schema)

internal fun addPortToFloatingIp(schema: Schema) =
    addPortFloatingIpRelation<FloatingIp, VirtualMachineInterface>(schema)

internal fun removeFloatingIpFromPort() =
    removePortFloatingIpRelation<VirtualMachineInterface, FloatingIp>(reversed = true)

internal fun removePortFromFloatingIp() =
    removePortFloatingIpRelation<FloatingIp, VirtualMachineInterface>()

private inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase>
    addPortFloatingIpRelation(schema: Schema) =
    addPortFloatingIpRelation(schema, Parent::class.java, Child::class.java)

private fun addPortFloatingIpRelation(schema: Schema, parentClass: ObjectClass, childClass: ObjectClass): WorkflowDefinition {
    val workflowName = schema.addRelationWorkflowName(parentClass, childClass)

    return workflow(workflowName)
    .inCategory(parentClass.pluginName)
    .withScriptFile("add${childClass.pluginName}To${parentClass.pluginName}") {
        parameter(item, parentClass.reference) {
            description = schema.parentDescriptionInCreateRelation(parentClass, childClass)
            mandatory = true
        }
        parameter(child, childClass.reference) {
            description = schema.childDescriptionInCreateRelation(parentClass, childClass, ignoreMissing = true)
            mandatory = true
        }
        parameter("fixedIpAddress", boolean) {
            description = propertyDescription<FloatingIp>(schema)
            defaultValue = true
            mandatory = true
        }
    }
}

private inline fun <reified Parent : ApiObjectBase, reified Child : ApiObjectBase>
    removePortFloatingIpRelation(reversed: Boolean = false) =
    removePortFloatingIpRelation(Parent::class.java, Child::class.java, reversed)

private fun removePortFloatingIpRelation(parentClass: ObjectClass, childClass: ObjectClass, reversed: Boolean): WorkflowDefinition {
    val workflowName = removeRelationWorkflowName(parentClass, childClass)
    val getter = if (reversed) childClass.backRefPropertyName else childClass.refPropertyName

    return workflow(workflowName)
    .inCategory(parentClass.pluginName)
    .withScriptFile("remove${childClass.pluginName}From${parentClass.pluginName}") {
        parameter(item, parentClass.reference) {
            description = parentDescriptionInRemoveRelation(parentClass, childClass)
            mandatory = true
        }
        parameter(child, childClass.reference) {
            description = childDescriptionInRemoveRelation(parentClass, childClass)
            mandatory = true
            visibility = WhenNonNull(item)
            browserRoot = item.asBrowserRoot()
            listedBy = actionCallTo(propertyValue).parameter(item).string(getter)
        }
    }
}
