package net.juniper.contrail.vro.workflows.dsl

sealed class ValidationCondition {
    abstract val stringCondition: String
}

object AlwaysValid : ValidationCondition() {
    override val stringCondition = "null"
}

class AlwaysInvalid(errorMesssage: String) : ValidationCondition() {
    override val stringCondition: String = "\"$errorMesssage\""
}

private class ValidationFromAction(actionCallBuilder: ActionCallBuilder) : ValidationCondition() {
    override val stringCondition = actionCallBuilder.create().ognl
}

private class ValidationConditionConjunction(vararg conditions: ValidationCondition) : ValidationCondition() {
    // Successful validation returns null which is a falsy value in ognl
    // Unsuccessful validation returns string which is a truthy value in ognl
    // && operator finishes evaluating on first falsy value and won't evaluate following validations
    // || on the other hand stops at first truthy value(which is an error) and returns it
    // Because we want all the conditions to be met, we use || operator
    override val stringCondition: String = conditions.joinToString(" || ") { "(${it.stringCondition})" }
}

infix fun ValidationCondition.and(other: ValidationCondition): ValidationCondition = when {
    this == AlwaysValid -> other
    other == AlwaysValid -> this
    else -> ValidationConditionConjunction(this, other)
}

fun ActionCallBuilder.asValidationCondition(): ValidationCondition {
    return ValidationFromAction(this)
}