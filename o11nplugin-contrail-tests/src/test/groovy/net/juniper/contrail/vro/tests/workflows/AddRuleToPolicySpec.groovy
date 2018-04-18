package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper

class AddRuleToPolicySpec extends WorkflowSpec {

    def addRuleToNetworkPolicy = engine.getFunctionFromWorkflowScript(workflows, "Add rule to network policy")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailPolicyRuleType = Java.type('net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper');")
        engine.engine.eval("var ContrailPolicyEntriesType = Java.type('net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper');")
        engine.engine.eval("var ContrailActionListType = Java.type('net.juniper.contrail.vro.gen.ActionListType_Wrapper');")
    }

    def mockParent = Mock(NetworkPolicy_Wrapper)
    def mockEntries = Mock(PolicyEntriesType_Wrapper)

    def someSimpleAction = "pass"
    def someProtocol = "tcp"
    def someDirection = "<>"
    def someAddressType = "Network"
    def someNetworkType = "any"
    def someNetwork = null
    def someCIDR = null
    def someSecurityGroup = null
    def someNetworkPolicy= null
    def somePorts = "any"
    def someLog = false
    def someDefineServices = false
    def someDefineMirror = false
    def someServiceInstances = null
    def someMirrorType = null
    def someAnalyzerInstance = null
    def someAnalyzerName = null
    def someNicAssistedVlan = null
    def someAnalyzerIP = null
    def someAnalyzerMac = null
    def someUdpPort = null
    def someJuniperHeader = null
    def someRoutingInstance = null
    def someNexthopMode = null
    def someVtepDestIP = null
    def someVtepDestMac = null
    def someVni = null



    def "Adding rule to a network policy with existing rule list"() {
        given: "A correct set of attributes"
        mockParent.getEntries() >> mockEntries

        when: "Running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addRuleToNetworkPolicy,
            mockParent,
            someSimpleAction,
            someProtocol,
            someDirection,
            someAddressType,
            someCIDR,
            someNetworkType,
            someNetwork,
            someNetworkPolicy,
            someSecurityGroup,
            somePorts,
            someAddressType,
            someCIDR,
            someNetworkType,
            someNetwork,
            someNetworkPolicy,
            someSecurityGroup,
            somePorts,
            someLog,
            someDefineServices,
            someDefineMirror,
            someServiceInstances,
            someMirrorType,
            someAnalyzerInstance,
            someAnalyzerName,
            someNicAssistedVlan,
            someAnalyzerIP,
            someAnalyzerMac,
            someUdpPort,
            someJuniperHeader,
            someRoutingInstance,
            someNexthopMode,
            someVtepDestIP,
            someVtepDestMac,
            someVni
        )

        // There is no way to mock the constructor, so checking if constructor is invoked with correct parameters is hard.
        then: "A new rule entry matching the input parameters should be added to the parent policy."
        1 * mockEntries.addPolicyRule({
                it.protocol == someProtocol})

        and: "The parent policy should be updated."
        1 * mockParent.update()
    }
}
