package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.tests.ScriptTestEngine
import net.juniper.contrail.vro.tests.actions.ValidationAsserts
import net.juniper.contrail.vro.workflows.dsl.AlwaysValidated
import net.juniper.contrail.vro.workflows.dsl.ValidationConditionFromString
import net.juniper.contrail.vro.workflows.dsl.ValidationConditionKt
import spock.lang.Specification

class ValidationAndSpec extends Specification implements ValidationAsserts{
    def errorMessage = "Error message"
    def anotherErrorMessage = "Another error message"
    def scriptTestEngine = new ScriptTestEngine()

    def "error message and null results in error message"() {
        given: "arguments"
        def firstArgument = new ValidationConditionFromString(errorMessage)
        def secondArgument = new AlwaysValidated()
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is unsuccessful with error message"
        validationFailureWith(result, errorMessage)
    }

    def "null and error message results in error message"() {
        given: "arguments"
        def firstArgument = new AlwaysValidated()
        def secondArgument = new ValidationConditionFromString(errorMessage)
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is unsuccessful with error message"
        validationFailureWith(result, errorMessage)
    }

    def "error message and another error message results in first error message"() {
        given: "arguments"
        def firstArgument = new ValidationConditionFromString(errorMessage)
        def secondArgument = new ValidationConditionFromString(anotherErrorMessage)
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is unsuccessful with first error message"
        validationFailureWith(result, errorMessage)
    }

    def "successful validation and successful validation results in successful validation"() {
        given: "arguments"
        def firstArgument = new AlwaysValidated()
        def secondArgument = new AlwaysValidated()
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is successful"
        validationSuccess(result)
    }

}