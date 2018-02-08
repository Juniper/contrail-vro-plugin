/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

sealed class VisibilityCondition {
    abstract val stringCondition: String
}

object AlwaysVisible : VisibilityCondition() {
    override val stringCondition: String = "true"
}

class WhenNonNull(name: String) : VisibilityCondition() {
    override val stringCondition: String = "#$name != null"
}

class FromBooleanParameter(name: String) : VisibilityCondition() {
    override val stringCondition: String = "#$name"
}

class FromStringParameter(name: String, value: String) : VisibilityCondition() {
    override val stringCondition: String = "#$name == \"$value\""
}

class ConditionConjunction(vararg conditions: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = conditions.joinToString(" && ") { "(${it.stringCondition})" }
}

class ConditionAlternative(vararg conditions: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = conditions.joinToString(" || ") { "(${it.stringCondition})" }
}