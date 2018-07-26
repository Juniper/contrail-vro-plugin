/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import static net.juniper.contrail.vro.config.Actions.isValidAllocactionPool

class AllocationPoolValidationSpec extends ActionSpec implements ValidationAsserts{
    def validatePool = actionFromScript(isValidAllocactionPool)
    def allocationValidationMessage = "e.g. 192.168.2.3-192.168.2.10 <enter>... and IPs should be from CIDR"

    def "validating allocation pool with pools and cidr not defined should pass" () {
        given: "cidr and pools parameters set to null"
        def cidr = null
        def pools = null

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating allocation pool with pools not defined should pass" () {
        given: "empty cidr and pools parameter set to null"
        def cidr = "      "
        def pools = null

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating allocation pool with cidr not defined should pass" () {
        given: "empty pools strings and cidr parameter set to null"
        def cidr = null
        def pools = ["  ", "   "]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating allocation pool with cidr not defined and pools with length 0 should pass" () {
        given: "empty pools array and cidr parameter set to null"
        def cidr = null
        def pools = []

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating allocation pool with valid IPv4 pools and cidr should pass" () {
        given: "valid IPv4 pools and cidr"
        def cidr = "192.168.0.2/24"
        def pools = ["192.168.0.2-192.168.0.10", "192.168.0.15-192.168.0.100"]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating pools with valid IPv4 pools and cidr with preceding and trailing whitespaces should pass" () {
        given: "valid IPv4 pools and cidr with preceding and trailing whitespaces"
        def cidr = "   192.169.0.2/12    "
        def pools = ["   192.169.0.2-192.169.0.10   ", "   192.169.0.15-192.169.0.100   "]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating allocation pool with valid IPv6 pools and cidr should pass" () {
        given: "valid IPv6 pools and cidr"
        def cidr = "2001::/64"
        def pools = ["2001::1-2001::6", "2001::7-2001::F"]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating allocation pool with not valid cidr format should not pass" () {
        given: "valid pools and cidr with wrong mask"
        def cidr = "192.168.0.2/234"
        def pools = ["192.168.0.2-192.168.0.10", "192.168.0.15-192.168.0.100"]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, allocationValidationMessage)
    }

    def "validating allocation pool with overlapping pools should not pass" () {
        given: "overlapping pools and valid cidr"
        def cidr = "192.168.0.2/23"
        def pools = ["192.168.0.2-192.168.0.10", "192.168.0.10-192.168.0.100"]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, allocationValidationMessage)
    }

    def "validating allocation pool with pools no in cidr should not pass" () {
        given: "overlapping pools and valid cidr"
        def cidr = "192.168.0.2/25"
        def pools = ["192.168.0.2-192.168.0.10", "192.168.0.10-192.168.0.190"]

        when: "executing validating script"
        def result = engine.invokeFunction(validatePool, pools, cidr)

        then: "it fails with the correct message"
        validationFailureWith(result, allocationValidationMessage)
    }
}
