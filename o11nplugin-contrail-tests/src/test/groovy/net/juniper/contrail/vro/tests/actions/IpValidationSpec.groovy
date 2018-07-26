/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import static net.juniper.contrail.vro.config.Actions.isValidIp

class IpValidationSpec extends ActionSpec implements ValidationAsserts{
    def validateIp = actionFromScript(isValidIp)
    def ipValidationMessage = "Enter valid IPv4 or IPv6 Address"

    def "validating ip not defined should pass" () {
        given: "ip parameter set to null"
        def ip = null

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating empty ip should not pass" () {
        given: "empty ip"
        def ip = "      "

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns error message"
        validationFailureWith(result, ipValidationMessage)
    }

    def "validating private IPv4 ip should pass" () {
        given: "valid private IPv4 address"
        def ip = "192.168.0.2"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating public IPv4 ip should pass" () {
        given: "valid public IPv4"
        def ip = "19.117.63.253"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating broadcast IPv4 ip should pass" () {
        given: "IPv4 broadcast address"
        def ip = "255.255.255.255"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating all-zero IPv4 ip should pass" () {
        given: "all-zero IPv4 address"
        def ip = "0.0.0.0"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating incorrect IPv4 ip should not pass" () {
        given: "IPv4 address with octet value grater than 255"
        def ip = "192.256.0.2"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns error message"
        validationFailureWith(result, ipValidationMessage)
    }

    def "validating not shortened IPv6 ip address should pass" () {
        given: "valid not shortened IPv6 address"
        def ip = "2001:0db8:0000:0000:0000:ff00:0042:8329"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating IPv6 address without leading zeros should pass" () {
        given: "valid IPv6 address without leading zeros"
        def ip = "2001:db8:0:0:0:ff00:42:8329"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating fully abbreviated IPv6 address should pass" () {
        given: "valid fully abbreviated IPv6 address"
        def ip = "2001:db8::ff00:42:8329"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating all-zero IPv6 ip should pass" () {
        given: "all-zero IPv6 address"
        def ip = "::"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns null"
        validationSuccess(result)
    }

    def "validating incorrect IPv6 ip should not pass" () {
        given: "IPv6 address with single hexadecimal value greater than F"
        def ip = "::G"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip)

        then: "it returns error message"
        validationFailureWith(result, ipValidationMessage)
    }
}
