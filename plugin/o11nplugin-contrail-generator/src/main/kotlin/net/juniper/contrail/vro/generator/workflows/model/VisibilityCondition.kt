/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

sealed class VisibilityCondition

data class BooleanVisibilityCondition(val name: String) : VisibilityCondition()

data class StringVisibilityCondition(val name: String, val value: String) : VisibilityCondition()
