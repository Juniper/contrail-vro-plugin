/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import static net.juniper.contrail.vro.config.Actions.isInCidr

class InCidrValidationSpec extends ActionSpec implements ValidationAsserts{
    def validateIp = actionFromScript(isInCidr)
    def ipValidationMessage = "IP must be in defined CIDR"

    def "validating ip with ip and cidr not defined should pass" () {
        given: "cidr and ip parameters set to null"
        def cidr = null
        def ip = null

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating ip with ip not defined and empty cidr should pass" () {
        given: "empty cidr and ip parameter set to null"
        def cidr = "      "
        def ip = null

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating ip with valid IPv4 ip and cidr should pass" () {
        given: "valid IPv4 ip and cidr"
        def cidr = "192.168.0.2/24"
        def ip = "192.168.0.0"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating ip with valid IPv6 ip and cidr should pass" () {
        given: "valid ip and cidr"
        def cidr = "2001:db8:85a3::/64"
        def ip = "2001:0db8:85a3::8a2e:370:7334"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating ip with valid IPv4 cidr and valid Ipv6 ip should not pass" () {
        given: "valid IPv4 cidr and valid Ipv6 ip"
        def cidr = "192.168.0.2/16"
        def ip = "2001::ab"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, ipValidationMessage)
    }

    def "validating ip with not valid cidr format should not pass" () {
        given: "valid ip and cidr with mask greater than 30"
        def cidr = "192.168.0.2/32"
        def ip = "192.168.0.2"

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, ipValidationMessage)
    }

    def "validating ip with trailing and preceding whitespaces should pass" () {
        given: "cidr and ip with trailing and preceding whitespaces"
        def cidr = "    192.168.0.2/30  "
        def ip = "   192.168.0.2    "

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating ip with whitespaces inside should not pass" () {
        given: "cidr and ip with trailing and preceding whitespaces and middle whitespaces in ip"
        def cidr = "    192.168.0.2/30  "
        def ip = "   192.168. 0.2    "

        when: "executing validating script"
        def result = engine.invokeFunction(validateIp, ip, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, ipValidationMessage)
    }
}
