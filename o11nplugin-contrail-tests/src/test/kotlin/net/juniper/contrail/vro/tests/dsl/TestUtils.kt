/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.workflows.model.WorkflowItemType

fun visualDefaultVerticalPosition(type: WorkflowItemType, defaultY: Float): Float =
    when (type) {
        WorkflowItemType.task -> defaultY + 10.0f
        else -> defaultY
}