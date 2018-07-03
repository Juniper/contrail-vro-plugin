/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.model.WorkflowItemType

fun visualVerticalPosition(type: WorkflowItemType, y: Float): Float =
    when (type) {
        WorkflowItemType.task -> y + 10.0f
        else -> y
}