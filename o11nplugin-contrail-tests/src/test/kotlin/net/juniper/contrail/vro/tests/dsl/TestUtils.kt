/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.dsl.defaultY
import net.juniper.contrail.vro.workflows.model.WorkflowItemType

fun trueVerticalPosition(type: WorkflowItemType): Float {
    if (type == WorkflowItemType.task) return defaultY + 10.0f
    return defaultY
}