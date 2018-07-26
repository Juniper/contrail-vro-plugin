/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import static net.juniper.contrail.vro.config.Actions.isValidSubnet

class CidrValidationSpec extends ActionSpec implements ValidationAsserts{
    def validateCidr = actionFromScript(isValidSubnet)
    def cidrValidationMessage = "Enter valid IPv4 or IPv6 Subnet/Mask"

    def "validating cidr not defined should pass" () {
        given: "cidr parameter set to null"
        def cidr = null

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating empty cidr should not pass" () {
        given: "empty cidr"
        def cidr = "      "

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it returns error message"
        validationFailureWith(result, cidrValidationMessage)
    }

    def "validating IPv4 cidr should pass" () {
        given: "valid cidr"
        def cidr = "192.168.0.2/24"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating IPv6 cidr should pass" () {
        given: "valid IPv6 cidr"
        def cidr = "2001:db8:85a3::/64"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating valid IPv4 cidr with too big mask should not pass" () {
        given: "IPv4 cidr with mask greater than 30"
        def cidr = "192.168.0.2/31"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, cidrValidationMessage)
    }

    def "validating valid IPv6 cidr with too big mask should not pass" () {
        given: "IPv6 cidr with mask greater than 126"
        def cidr = "2001::/127"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, cidrValidationMessage)
    }

    def "validating valid IPv4 cidr with biggest valid mask should pass" () {
        given: "IPv4 cidr with 30 bit mask"
        def cidr = "192.168.0.2/30"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating valid IPv6 cidr with biggest valid mask should  pass" () {
        given: "IPv6 cidr with 126 bit mask"
        def cidr = "2001::/126"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating valid IPv4 cidr with smallest valid mask should pass" () {
        given: "IPv4 cidr with 0 bit mask"
        def cidr = "192.168.0.2/0"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating valid IPv6 cidr with smallest valid mask should  pass" () {
        given: "IPv6 cidr with 0 bit mask"
        def cidr = "2001::/0"

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating cidr with trailing and preceding whitespaces should pass" () {
        given: "cidr with trailing and preceding whitespaces "
        def cidr = "    192.168.0.2/10  "

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating cidr with whitespaces inside should not pass" () {
        given: "cidr with trailing, preceding and middle whitespaces "
        def cidr = "    192.168 .0.2/30  "

        when: "executing validating script"
        def result = engine.invokeFunction(validateCidr, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, cidrValidationMessage)
    }
}
