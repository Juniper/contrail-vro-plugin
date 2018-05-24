package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.SubnetListType
import net.juniper.contrail.api.types.SubnetType
import static net.juniper.contrail.vro.model.UtilsKt.utils
import static net.juniper.contrail.vro.config.Actions.addressGroupSubnets

class AddressGroupSubnetsSpec extends ActionSpec {
    def action = actionFromScript(addressGroupSubnets)

    def "null address group results in null" () {
        given: "null address group"
        def addressGroup = null

        when: "retrieved subnet list"
        def result = invokeAction(action, addressGroup)

        then: "resulting list is null"
        result == null
    }

    def "empty address group results in null" () {
        given: "empty address group"
        def addressGroup = new AddressGroup()

        when: "retrieved subnet list"
        def result = invokeAction(action, addressGroup)

        then: "resulting list is null"
        result == null
    }

    def "address group with empty subnet list results in empty list" () {
        given: "empty address group"
        def addressGroup = new AddressGroup()
        def subnetListType = new SubnetListType()
        addressGroup.prefix = subnetListType
        subnetListType.subnet = new ArrayList<>()

        when: "retrieved subnet list"
        def result = invokeAction(action, addressGroup) as List<String>

        then: "resulting list is empty"
        result.isEmpty()
    }

    def "address group with single subnet results in list with single formatted subnet" () {
        given: "address group with single subnet"
        def addressGroup = new AddressGroup()
        def subnetListType = new SubnetListType()
        addressGroup.prefix = subnetListType
        def somePrefix = "1.2.3.4"
        def somePrefixLen = 16
        def subnet = new SubnetType(somePrefix, somePrefixLen)
        subnetListType.addSubnet(subnet)

        when: "retrieved subnet list"
        def result = invokeAction(action, addressGroup) as List<String>

        then: "resulting list has one formatted element"
        result.size() == 1
        result[0] == utils.subnetToString(subnet)
    }
}
