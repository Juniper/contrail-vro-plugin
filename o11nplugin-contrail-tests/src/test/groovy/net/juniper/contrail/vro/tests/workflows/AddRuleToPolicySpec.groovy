package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.api.Status
import org.spockframework.mock.MockUtil

class AddRuleToPolicySpec extends WorkflowSpec {

    def addRuleToNetworkPolicy = engine.getFunctionFromWorkflowScript(workflows, "Add rule to network policy")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailPolicyRuleType = Java.type('net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper');")
        engine.engine.eval("var ContrailPolicyEntriesType = Java.type('net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper');")
        engine.engine.eval("var ContrailActionListType = Java.type('net.juniper.contrail.vro.gen.ActionListType_Wrapper');")
    }

    def mockUtil = new MockUtil()

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
        def dependencies = createContextAndDependencies()
        def networkPolicy = dependencies.someNetworkPolicy()

        mockUtil.attachMock(DetachedMocksKt.apiConnectorMock, this)
        DetachedMocksKt.apiConnectorMock.read(_) >> Status.success()

        when: "Running the script"
        loadWrapperTypes()

        engine.invokeFunction(
            addRuleToNetworkPolicy,
            networkPolicy,
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

        // TODO: check that the rule has been added
        then: "The parent policy should be updated."
        1 * DetachedMocksKt.apiConnectorMock.update({it.uuid == networkPolicy.uuid}) >> Status.success()
    }
}
