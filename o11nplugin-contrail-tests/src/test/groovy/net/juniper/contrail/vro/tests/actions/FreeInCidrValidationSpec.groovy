/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import static net.juniper.contrail.vro.config.Actions.isFreeInCidr

class FreeInCidrValidationSpec extends ActionSpec implements ValidationAsserts{
    def validateFreeIp = actionFromScript(isFreeInCidr)
    def freeIpMsg = "Default Gateway IP must be in CIDR and not be in allocation pools or be the same as DNS server IP"

    def "validating free ip in cidr with all parameters not defined should pass" () {
        given: "cidr, ip, dns and pools parameters set to null"
        def cidr = null
        def ip = null
        def dns = null
        def pools = null

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating free ip in cidr with any of mandatory parameters not defined should pass" () {
        given: "cidr parameter set to null and ip, dns and pools parameters with valid values"
        def cidr = null
        def ip = "192.168.0.1"
        def dns = "192.168.0.6"
        def pools = ["192.168.0.4-192.168.0.10"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating free ip in cidr with all valid IPv4 values should pass" () {
        given: "cidr, ip, dns and pools parameters with valid IPv4 values"
        def cidr = "192.168.0.0/24"
        def ip = "192.168.0.1"
        def dns = "192.168.0.6"
        def pools = ["192.168.0.4-192.168.0.10"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating free ip in cidr with all valid IPv6 values should pass" () {
        given: "cidr, ip, dns and pools parameters with valid IPv6 values"
        def cidr = "2001::/64"
        def ip = "2001::1"
        def dns = "2001::4"
        def pools = ["2001::5-2001::8", "2001::a-2001::ba"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating free ip in cidr with non-mandatory values not defined should pass" () {
        given: "dns and pools parameters set to null and ip and cidr parameters with valid values"
        def cidr = "192.168.0.0/24"
        def ip = "192.168.0.1"
        def dns = null
        def pools = null

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating free ip in cidr with parameters with redundant whitespaces should pass" () {
        given: "mandatory parameters with trailing and preceding whitespaces and non-mandatory blank"
        def cidr = "   192.168.0.0/24   "
        def ip = "   192.168.0.1  "
        def dns = "     "
        def pools = ["     "]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "validating free ip in cidr with parameters with mismatched ip version should not pass" () {
        given: "valid ip parameter in IPv6 and other parameters valid in IPv4"
        def cidr = "192.168.0.0/24"
        def ip = "::ffff:c0a8:1"
        def dns = "192.168.0.6"
        def pools = ["192.168.0.4-192.168.0.10"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it fails with the correct message"
        validationFailureWith(result, freeIpMsg)
    }

    def "validating free ip in cidr with ip not in cidr should not pass" () {
        given: "cidr, ip, dns and pools parameters with valid IPv4 values but ip not in cidr"
        def cidr = "192.168.0.0/24"
        def ip = "192.169.0.1"
        def dns = "192.168.0.6"
        def pools = ["192.168.0.4-192.168.0.10"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it fails with the correct message"
        validationFailureWith(result, freeIpMsg)
    }

    def "validating free ip in cidr with ip in allocation pool should not pass" () {
        given: "cidr, ip, dns and pools parameters with valid IPv4 values but ip in allocation pool"
        def cidr = "192.168.0.0/24"
        def ip = "192.168.0.25"
        def dns = "192.168.0.6"
        def pools = ["192.168.0.1-192.168.0.10", "192.168.0.20-192.168.0.30"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it fails with the correct message"
        validationFailureWith(result, freeIpMsg)
    }

    def "validating free ip in cidr with ip the same as dns should not pass" () {
        given: "cidr, ip, dns and pools parameters with valid IPv4 values but ip equals dns"
        def cidr = "192.168.0.0/24"
        def ip = "192.168.0.2"
        def dns = "192.168.0.2"
        def pools = ["192.168.0.5-192.168.0.10", "192.168.0.20-192.168.0.30"]

        when: "executing validating script"
        def result = engine.invokeFunction(validateFreeIp, ip, cidr, pools, dns)

        then: "it fails with the correct message"
        validationFailureWith(result, freeIpMsg)
    }
}
