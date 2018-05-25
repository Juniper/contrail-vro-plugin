package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.FirewallRuleEndpointType_Wrapper

class SomeSpec extends WorkflowSpec {

    def "testingStuff"(){
        given: "some firewall rule"
        def x = dependencies.someFirewallRule()
        def y = new FirewallRuleEndpointType_Wrapper()
        y.setVirtualNetwork("some:virtual:network")
        x.setEndpoint1(y)

        when: "getting its innards"
        def r = x.endpointType(1)

        then: "its innards are alright"
        r == "virtualNetwork"
    }
}
