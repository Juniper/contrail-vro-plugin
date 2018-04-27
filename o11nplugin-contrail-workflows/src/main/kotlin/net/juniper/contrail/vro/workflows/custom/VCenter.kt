/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.custom

import net.juniper.contrail.api.types.VirtualMachineInterface
import net.juniper.contrail.vro.config.constants.Connection
import net.juniper.contrail.vro.config.constants.VC
import net.juniper.contrail.vro.config.constants.VirtualMachine
import net.juniper.contrail.vro.config.portOfVCVirtualMachine
import net.juniper.contrail.vro.workflows.dsl.ofType
import net.juniper.contrail.vro.workflows.model.Reference
import net.juniper.contrail.vro.workflows.model.number
import net.juniper.contrail.vro.workflows.model.reference

val portOfVCVirtualMachineAction = ActionDefinition(
    name = portOfVCVirtualMachine,
    resultType = reference<VirtualMachineInterface>(),
    parameters = listOf("connection" ofType Reference(Connection),
        "vcvm" ofType Reference(VirtualMachine, VC),
        "timeout" ofType number)
)