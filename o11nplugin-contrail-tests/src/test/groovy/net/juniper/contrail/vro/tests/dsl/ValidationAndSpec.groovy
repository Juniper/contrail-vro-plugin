package net.juniper.contrail.vro.tests.dsl

import net.juniper.contrail.vro.tests.ScriptTestEngine
import net.juniper.contrail.vro.tests.actions.ValidationAsserts
import net.juniper.contrail.vro.workflows.dsl.AlwaysValid
import net.juniper.contrail.vro.workflows.dsl.AlwaysInvalid
import net.juniper.contrail.vro.workflows.dsl.ValidationConditionKt
import spock.lang.Specification

class ValidationConjunctionSpec extends Specification implements ValidationAsserts{
    def errorMessage = "Error message"
    def anotherErrorMessage = "Another error message"
    def scriptTestEngine = new ScriptTestEngine()

    def "error message and null results in error message"() {
        given: "arguments"
        def firstArgument = new AlwaysInvalid(errorMessage)
        def secondArgument = new AlwaysValid()
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is unsuccessful with error message"
        validationFailureWith(result, errorMessage)
    }

    def "null and error message results in error message"() {
        given: "arguments"
        def firstArgument = new AlwaysValid()
        def secondArgument = new AlwaysInvalid(errorMessage)
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is unsuccessful with error message"
        validationFailureWith(result, errorMessage)
    }

    def "error message and another error message results in first error message"() {
        given: "arguments"
        def firstArgument = new AlwaysInvalid(errorMessage)
        def secondArgument = new AlwaysInvalid(anotherErrorMessage)
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is unsuccessful with first error message"
        validationFailureWith(result, errorMessage)
    }

    def "successful validation and successful validation results in successful validation"() {
        given: "arguments"
        def firstArgument = new AlwaysValid()
        def secondArgument = new AlwaysValid()
        when: "conditions are evaluated"
        def complexCondition = ValidationConditionKt.and(firstArgument, secondArgument)
        def result = scriptTestEngine.evalCondition(complexCondition.stringCondition)
        then: "validation is successful"
        validationSuccess(result)
    }

}