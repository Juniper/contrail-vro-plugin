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

private class VisibilityFromAction(actionCallBuilder: ActionCallBuilder) : VisibilityCondition() {
    override val stringCondition: String = actionCallBuilder.create().ognl
}

fun ActionCallBuilder.asVisibilityCondition(): VisibilityCondition =
    VisibilityFromAction(this)

private class ConditionConjunction(vararg conditions: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = conditions.joinToString(" && ") { "(${it.stringCondition})" }
}

private class ConditionAlternative(vararg conditions: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = conditions.joinToString(" || ") { "(${it.stringCondition})" }
}

private class ConditionNegation(condition: VisibilityCondition) : VisibilityCondition() {
    override val stringCondition: String = "!(${condition.stringCondition})"
}

infix fun VisibilityCondition.and(other: VisibilityCondition): VisibilityCondition = when {
    this == AlwaysVisible -> other
    other == AlwaysVisible -> this
    else -> ConditionConjunction(this, other)
}

infix fun VisibilityCondition.or(other: VisibilityCondition): VisibilityCondition = when {
    this == AlwaysVisible -> this
    other == AlwaysVisible -> other
    else -> ConditionAlternative(this, other)
}

operator fun VisibilityCondition.not(): VisibilityCondition =
    ConditionNegation(this)