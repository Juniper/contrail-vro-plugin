package net.juniper.contrail.vro.tests.utils

import net.juniper.contrail.vro.model.Utils
import spock.lang.Specification

class ParseSubnetSpec extends Specification {
    def utils = new Utils()
    def someIP = "1.2.3.4"
    def somePrefix = 16
    def someDomain = "domainName"
    def someProject = "projectName"
    def someNetwork = "networkName"

    def "Paring subnet should return null when receiving null"() {
        when:
        def result = utils.parseSubnet(null)

        then:
        result == null
    }

    def "Paring subnet should return null for blank string"() {
        when:
        def result = utils.parseSubnet("")

        then:
        result == null
    }

    def "Subnet in CIDR format should be correctly parsed"() {
        given: "Subnet in CIDR format"
        def subnet = "$someIP/$somePrefix"

        when:
        def result = utils.parseSubnet(subnet)

        then:
        result.ipPrefix == someIP
        result.ipPrefixLen == somePrefix
    }

    def "Subnet in CIDR format should fail if CIDR is invalid"() {
        given: "Subnet in CIDR format with invalid CIDR"
        def subnet = "$somePrefix/$someIP"

        when:
        utils.parseSubnet(subnet)

        then:
        thrown(IllegalArgumentException)
    }

    def "Subnet in VN:CIDR format should be correctly parsed"() {
        given: "Subnet in VN:CIDR format"
        def subnetName = "$someNetwork:$someIP"
        def subnet = "$subnetName/$somePrefix"

        when:
        def result = utils.parseSubnet(subnet)

        then:
        result.ipPrefix == subnetName
        result.ipPrefixLen == somePrefix
    }

    def "Subnet in VN-FQNAME:CIDR format should be correctly parsed"() {
        given: "Subnet in VN-FQNAME:CIDR format"
        def subnetName = "$someDomain:$someProject:$someNetwork:$someIP"
        def subnet = "$subnetName/$somePrefix"

        when:
        def result = utils.parseSubnet(subnet)

        then:
        result.ipPrefix == subnetName
        result.ipPrefixLen == somePrefix
    }

    def "Subnet containing a 2-part FQN and CIDR should cause an error"() {
        given: "Subnet in FQNAME:CIDR format with 2-part FQN"
        def subnet = "$someProject:$someNetwork:$someIP/$somePrefix"

        when:
        utils.parseSubnet(subnet)

        then:
        thrown(IllegalArgumentException)
    }

    def "Subnet containing a 4-part FQN and CIDR should cause an error"() {
        given: "Subnet in FQNAME:CIDR format with 2-part FQN"
        def subnet = "$someDomain:$someProject:$someNetwork:something:$someIP/$somePrefix"

        when:
        utils.parseSubnet(subnet)

        then:
        thrown(IllegalArgumentException)
    }
}
