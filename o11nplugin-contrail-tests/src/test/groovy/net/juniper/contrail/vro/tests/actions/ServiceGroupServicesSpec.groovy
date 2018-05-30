/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.api.types.FirewallServiceGroupType
import net.juniper.contrail.api.types.FirewallServiceType
import net.juniper.contrail.api.types.PortType
import net.juniper.contrail.api.types.ServiceGroup

import static net.juniper.contrail.vro.config.Actions.serviceGroupServices
import static net.juniper.contrail.vro.model.UtilsKt.utils

class ServiceGroupServicesSpec extends ActionSpec implements ValidationAsserts {
    def actionScript = actionFromScript(serviceGroupServices)

    def "null service group results in empty list" () {
        given:
        def item = null

        when:
        def result = invokeAction(actionScript, item) as List<String>

        then:
        result.isEmpty()
    }

    def "null FirewallServiceGroupType object results in empty list" () {
        given:
        def item = new ServiceGroup()
        item.setFirewallServiceList(null)

        when:
        def result = invokeAction(actionScript, item) as List<String>

        then:
        result.isEmpty()
    }

    def "null firewall service list results in empty list" () {
        given:
        def item = new ServiceGroup()
        def listType = new FirewallServiceGroupType(null)
        item.setFirewallServiceList(listType)

        when:
        def result = invokeAction(actionScript, item) as List<String>

        then:
        result.isEmpty()
    }

    def "empty firewall service list results in empty list" () {
        given:
        def item = new ServiceGroup()
        def listType = new FirewallServiceGroupType(new ArrayList<FirewallServiceType>())
        item.setFirewallServiceList(listType)

        when:
        def result = invokeAction(actionScript, item) as List<String>

        then:
        result.isEmpty()
    }

    def "single service in the list results in list with single element formatted using Utils" () {
        given:
        def item = new ServiceGroup()
        def listType = new FirewallServiceGroupType()
        item.setFirewallServiceList(listType)
        def service = new FirewallServiceType("tcp", null, new PortType(0, 65535), new PortType(7, 131))
        listType.addFirewallService(service)

        when:
        def result = invokeAction(actionScript, item) as List<String>

        then:
        result.size() == 1
        result[0] == utils.firewallServiceToString(service, 0)
    }
}
