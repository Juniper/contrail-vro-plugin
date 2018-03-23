/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.dsl

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

// Name is not "FromAction" because of conflict with FromAction class used in data binding attributes.
class FromActionVisibility(actionCallBuilder: ActionCallBuilder) : VisibilityCondition() {
    override val stringCondition: String = actionCallBuilder.create().ognl
}

class ConditionConjunction(vararg conditions: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = conditions.filter { it != AlwaysVisible }.joinToString(" && ") { "(${it.stringCondition})" }
}

class ConditionAlternative(vararg conditions: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = conditions.joinToString(" || ") { "(${it.stringCondition})" }
}

class ConditionNegation(condition: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = "!(${condition.stringCondition})"
}