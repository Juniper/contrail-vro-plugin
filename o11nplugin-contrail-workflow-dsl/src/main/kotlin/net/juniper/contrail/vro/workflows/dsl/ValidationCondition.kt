package net.juniper.contrail.vro.workflows.dsl

sealed class ValidationCondition {
    abstract val stringCondition: String
}

object AlwaysValidated : ValidationCondition() {
    override val stringCondition = "null"
}

class ValidationConditionFromString(value: String) : ValidationCondition() {
    override val stringCondition: String = "\"$value\""
}

private class ValidationFromAction(actionCallBuilder: ActionCallBuilder) : ValidationCondition() {
    override val stringCondition = actionCallBuilder.create().ognl
}

private class ValidationConditionAlternative(vararg conditions: ValidationCondition) : ValidationCondition() {
    override val stringCondition: String = conditions.joinToString(" || ") { "(${it.stringCondition})" }
}

infix fun ValidationCondition.and(other: ValidationCondition): ValidationCondition = when {
    this == AlwaysValidated -> this
    other == AlwaysValidated -> other
    else -> ValidationConditionAlternative(this, other)
}

fun ActionCallBuilder.asValidationCondition(): ValidationCondition {
    return ValidationFromAction(this)
}