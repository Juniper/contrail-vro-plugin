/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

sealed class VisibilityCondition

object AlwaysVisible : VisibilityCondition()

class WhenNonNull(val name: String) : VisibilityCondition()

class FromBooleanParameter(val name: String) : VisibilityCondition()

class FromStringParameter(val name: String, val value: String) : VisibilityCondition()
