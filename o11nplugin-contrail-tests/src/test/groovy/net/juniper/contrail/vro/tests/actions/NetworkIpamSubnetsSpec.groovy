/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.api.types.IpamSubnetType
import net.juniper.contrail.api.types.IpamSubnets
import net.juniper.contrail.api.types.NetworkIpam
import net.juniper.contrail.api.types.SubnetType

import static net.juniper.contrail.vro.config.Actions.networkIpamSubnets
import static net.juniper.contrail.vro.model.UtilsKt.utils

class NetworkIpamSubnetsSpec extends ActionSpec {
    def action = actionFromScript(networkIpamSubnets)

    def "null network IPAM results in null" () {
        given: "null IPAM"
        def ipam = null

        when: "retrieved subnet list"
        def result = invokeAction(action, ipam)

        then: "resulting list is null"
        result == null
    }

    def "empty IPAM results in null" () {
        given: "empty IPAM"
        def ipam = new NetworkIpam()

        when: "retrieved subnet list"
        def result = invokeAction(action, ipam)

        then: "resulting list is null"
        result == null
    }

    def "IPAM with empty subnet list results in empty list" () {
        given: "empty IPAM"
        def ipam = new NetworkIpam()
        def ipamSubnets = new IpamSubnets()
        ipam.ipamSubnets = ipamSubnets
        ipamSubnets.subnets = new ArrayList<>()

        when: "retrieved subnet list"
        def result = invokeAction(action, ipam) as List<String>

        then: "resulting list is empty"
        result.isEmpty()
    }

    def "IPAM with single subnet results in list with single formatted subnet" () {
        given: "IPAM with single subnet"
        def ipam = new NetworkIpam()
        def ipamSubnets = new IpamSubnets()
        ipam.ipamSubnets = ipamSubnets
        def somePrefix = "1.2.3.4"
        def somePrefixLen = 16
        def ipamSubnet = new IpamSubnetType()
        def subnet = new SubnetType(somePrefix, somePrefixLen)
        ipamSubnet.setSubnet(subnet)
        ipamSubnets.addSubnets(ipamSubnet)

        when: "retrieved subnet list"
        def result = invokeAction(action, ipam) as List<String>

        then: "resulting list has one formatted element"
        result.size() == 1
        result[0] == utils.ipamSubnetToString(ipamSubnet)
    }
}
